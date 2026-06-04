package dev.docktor.docker;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.IOException;

public class DockerContextManager {
  private final CommandRunner commandRunner;

  public DockerContextManager(CommandRunner commandRunner) {
    this.commandRunner = commandRunner;
  }

  public DiagnosticMessage currentContext() {
    try {
      var result = commandRunner.run("docker", "context", "show");
      if (result.successful()) {
        return DiagnosticMessage.ok("Docker context: " + result.stdout());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      return contextUnavailable();
    }
    return contextUnavailable();
  }

  private DiagnosticMessage contextUnavailable() {
    return DiagnosticMessage.warning(
        "Docker context could not be read",
        "Docker CLI is missing or Docker context metadata is unavailable.",
        "Install Docker CLI and configure a Docktor VM context later.");
  }

  public DiagnosticMessage configureDocktorContext() {
    return DiagnosticMessage.warning(
        "Docker context setup is not implemented yet",
        "Real VM socket wiring is outside the first milestone.",
        "Create a Docker context after the Lima VM exposes a Docker socket.");
  }
}
