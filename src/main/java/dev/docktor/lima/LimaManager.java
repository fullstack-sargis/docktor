package dev.docktor.lima;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.IOException;

public class LimaManager {
  private final CommandRunner commandRunner;
  private final LimaConfigGenerator configGenerator;

  public LimaManager(CommandRunner commandRunner, LimaConfigGenerator configGenerator) {
    this.commandRunner = commandRunner;
    this.configGenerator = configGenerator;
  }

  public DiagnosticMessage start() {
    return DiagnosticMessage.warning(
        "Docktor VM start is not implemented yet",
        "Real VM creation is outside the first milestone.",
        "Implement config creation with LimaConfigGenerator and run limactl start through CommandRunner.");
  }

  public DiagnosticMessage stop() {
    return DiagnosticMessage.warning(
        "Docktor VM stop is not implemented yet",
        "Real VM lifecycle management is outside the first milestone.",
        "Run limactl stop docktor through CommandRunner after VM support is implemented.");
  }

  public DiagnosticMessage ssh() {
    return DiagnosticMessage.warning(
        "Docktor VM ssh is not implemented yet",
        "Real VM lifecycle management is outside the first milestone.",
        "Run limactl shell docktor through CommandRunner after VM support is implemented.");
  }

  public DiagnosticMessage status() {
    try {
      var result = commandRunner.run("limactl", "list", "docktor");
      if (result.successful()) {
        return DiagnosticMessage.ok("Lima VM status: " + result.stdout());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      return statusUnavailable();
    }
    return statusUnavailable();
  }

  private DiagnosticMessage statusUnavailable() {
    return DiagnosticMessage.warning(
        "Docktor Lima VM status is unavailable",
        "The VM may not exist yet, Lima may be missing, or VM support has not been implemented.",
        "Run docktor doctor and install Lima if missing.");
  }

  public String previewConfig(String architecture) {
    return configGenerator.generateDefaultConfig(architecture);
  }
}
