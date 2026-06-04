package dev.docktor.qemu.runtime;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.qemu.QemuCommandFailure;
import dev.docktor.qemu.QemuConfigGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class QemuProcessManager {
  private final CommandRunner commandRunner;
  private final QemuConfigGenerator configGenerator;

  public QemuProcessManager(CommandRunner commandRunner, QemuConfigGenerator configGenerator) {
    this.commandRunner = commandRunner;
    this.configGenerator = configGenerator;
  }

  public void start() throws IOException {
    if (isRunning()) {
      return;
    }

    Files.deleteIfExists(pidPath());

    var process =
        new ProcessBuilder(configGenerator.startCommand())
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(configGenerator.logPath().toFile()))
            .start();

    Files.writeString(pidPath(), Long.toString(process.pid()));
  }

  public DiagnosticMessage stop() {
    try {
      if (!isRunning()) {
        return DiagnosticMessage.warning(
            "QEMU VM is not running",
            "PID file does not exist: " + pidPath(),
            "Run `docktor start`.");
      }

      var pid = readPid();
      var result = commandRunner.run("kill", pid);
      if (result.successful()) {
        Files.deleteIfExists(pidPath());
        return DiagnosticMessage.ok("QEMU VM stopped");
      }

      return DiagnosticMessage.warning(
          "QEMU VM could not be stopped",
          QemuCommandFailure.reason(result),
          "Stop the process manually or remove stale PID file: " + pidPath());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return DiagnosticMessage.warning(
          "QEMU VM stop was interrupted",
          "Docktor was interrupted while stopping QEMU.",
          "Run `docktor status`.");
    } catch (IOException e) {
      return DiagnosticMessage.warning(
          "QEMU VM could not be stopped",
          e.getMessage(),
          "Stop the process manually or remove stale PID file: " + pidPath());
    }
  }

  public boolean isRunning() {
    try {
      if (!Files.exists(pidPath())) {
        return false;
      }

      var result = commandRunner.run("kill", "-0", readPid());
      if (result.successful()) {
        return true;
      }

      Files.deleteIfExists(pidPath());
      return false;
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }

  public String readPid() {
    try {
      return Files.readString(pidPath()).trim();
    } catch (IOException e) {
      return "unknown";
    }
  }

  public Path pidPath() {
    return configGenerator.pidPath();
  }
}
