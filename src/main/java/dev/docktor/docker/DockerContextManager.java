package dev.docktor.docker;

import dev.docktor.core.CommandRunner;
import dev.docktor.core.ProcessResult;
import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DockerContextManager {
  public static final String DOCKTOR_CONTEXT_NAME = "docktor";
  public static final String DOCKTOR_SSH_HOST = "docktor-vm";
  public static final String DOCKTOR_DOCKER_HOST = "ssh://" + DOCKTOR_SSH_HOST;

  private final CommandRunner commandRunner;

  public DockerContextManager(CommandRunner commandRunner) {
    this.commandRunner = commandRunner;
  }

  public DiagnosticMessage currentContext() {
    try {
      var result = commandRunner.run("docker", "context", "show");
      if (result.successful()) {
        return DiagnosticMessage.ok("Docker context: " + result.stdout().trim());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      return contextUnavailable();
    }

    return contextUnavailable();
  }

  public DiagnosticMessage useDefaultContext() {
    try {
      var result = commandRunner.run("docker", "context", "use", "default");
      if (result.successful()) {
        return DiagnosticMessage.ok("Docker context is set to default");
      }

      return DiagnosticMessage.warning(
          "Docker context could not be reset",
          commandFailureReason(result),
          "Run: docker context use default");
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }

      return DiagnosticMessage.warning(
          "Docker context could not be reset", e.getMessage(), "Run: docker context use default");
    }
  }

  public DiagnosticMessage configureDocktorContext() {
    try {
      ensureSshConfig();

      recreateDocktorContext();

      var useResult = commandRunner.run("docker", "context", "use", DOCKTOR_CONTEXT_NAME);
      if (!useResult.successful()) {
        return DiagnosticMessage.warning(
            "Docker context could not be selected",
            commandFailureReason(useResult),
            "Run: docker context use docktor");
      }

      return DiagnosticMessage.ok("Docker context is set to docktor");
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      return DiagnosticMessage.warning(
          "Docker context could not be configured",
          e.getMessage(),
          "Install Docker CLI and retry `docktor docker context`.");
    }
  }

  public String contextName() {
    return DOCKTOR_CONTEXT_NAME;
  }

  private void recreateDocktorContext() throws IOException, InterruptedException {
    var inspectResult = commandRunner.run("docker", "context", "inspect", DOCKTOR_CONTEXT_NAME);

    if (inspectResult.successful()) {
      var removeResult = commandRunner.run("docker", "context", "rm", "-f", DOCKTOR_CONTEXT_NAME);
      if (!removeResult.successful()) {
        throw new IOException(
            "Docker context could not be removed: " + commandFailureReason(removeResult));
      }
    }

    var createResult =
        commandRunner.run(
            "docker",
            "context",
            "create",
            DOCKTOR_CONTEXT_NAME,
            "--docker",
            "host=" + DOCKTOR_DOCKER_HOST);

    if (!createResult.successful()) {
      throw new IOException(
          "Docker context could not be created: " + commandFailureReason(createResult));
    }
  }

  private void ensureSshConfig() throws IOException {
    var sshDir = Path.of(System.getProperty("user.home"), ".ssh");
    var configPath = sshDir.resolve("config");

    Files.createDirectories(sshDir);

    var block =
        """

            Host docktor-vm
              HostName 127.0.0.1
              Port 2222
              User docktor
              StrictHostKeyChecking no
              UserKnownHostsFile /dev/null
            """;

    var existing = Files.exists(configPath) ? Files.readString(configPath) : "";
    if (!existing.contains("Host " + DOCKTOR_SSH_HOST)) {
      Files.writeString(configPath, existing + block);
    }
  }

  private String commandFailureReason(ProcessResult result) {
    if (result.stderr() != null && !result.stderr().isBlank()) {
      return result.stderr();
    }

    if (result.stdout() != null && !result.stdout().isBlank()) {
      return result.stdout();
    }

    return "Command exited with code " + result.exitCode() + ".";
  }

  private DiagnosticMessage contextUnavailable() {
    return DiagnosticMessage.warning(
        "Docker context could not be read",
        "Docker CLI is missing or Docker context metadata is unavailable.",
        "Install Docker CLI or run: docker context use " + DOCKTOR_CONTEXT_NAME);
  }
}
