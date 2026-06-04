package dev.docktor.qemu.checks;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.qemu.QemuConfigGenerator;
import java.io.IOException;
import java.util.Optional;

public class QemuPrerequisitesChecker {
  private static final String QEMU_IMG = "qemu-img";
  private static final String HDIUTIL = "hdiutil";

  private final CommandRunner commandRunner;
  private final QemuConfigGenerator configGenerator;

  public QemuPrerequisitesChecker(
      CommandRunner commandRunner, QemuConfigGenerator configGenerator) {
    this.commandRunner = commandRunner;
    this.configGenerator = configGenerator;
  }

  public Optional<DiagnosticMessage> check() throws IOException, InterruptedException {
    var qemuBinary = configGenerator.qemuBinary();

    if (commandMissing(qemuBinary)) {
      return Optional.of(
          DiagnosticMessage.warning(
              "QEMU is not installed",
              "Required command " + qemuBinary + " was not found on PATH.",
              "brew install qemu"));
    }

    if (commandMissing(QEMU_IMG)) {
      return Optional.of(
          DiagnosticMessage.warning(
              "qemu-img is not installed",
              "Required command " + QEMU_IMG + " was not found on PATH.",
              "brew install qemu"));
    }

    if (commandMissing(HDIUTIL)) {
      return Optional.of(
          DiagnosticMessage.warning(
              "hdiutil is not available",
              "hdiutil is required to create the cloud-init NoCloud seed ISO on macOS.",
              "Run Docktor on macOS or ensure hdiutil is available on PATH."));
    }

    return Optional.empty();
  }

  private boolean commandMissing(String command) throws IOException, InterruptedException {
    return !commandRunner.run("which", command).successful();
  }
}
