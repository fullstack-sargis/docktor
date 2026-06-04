package dev.docktor.docker;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.IOException;

public class DockerManager {
  private final CommandRunner commandRunner;

  public DockerManager(CommandRunner commandRunner) {
    this.commandRunner = commandRunner;
  }

  public DiagnosticMessage dockerDaemonReachable() {
    try {
      if (commandRunner.run("docker", "info").successful()) {
        return DiagnosticMessage.ok("Docker daemon is reachable");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      return dockerDaemonUnavailable();
    }
    return dockerDaemonUnavailable();
  }

  private DiagnosticMessage dockerDaemonUnavailable() {
    return DiagnosticMessage.error(
        "Docker daemon is not reachable",
        "Docker Engine is not running, the Docker context is wrong, or the VM Docker socket is not available.",
        "Start the VM and configure the local Docker context to the Docktor VM.");
  }
}
