package dev.docktor.qemu.runtime;

import dev.docktor.core.CommandRunner;
import dev.docktor.core.ProcessResult;
import dev.docktor.qemu.QemuCommandFailure;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class VmReadinessChecker {
  private static final Duration READINESS_COMMAND_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration CLOUD_INIT_TIMEOUT = Duration.ofMinutes(5);

  private static final String SSH = "ssh";
  private static final String SSH_PORT = "2222";
  private static final String SSH_TARGET = "docktor@127.0.0.1";

  private final CommandRunner commandRunner;
  private final int readinessAttempts;
  private final Duration readinessSleep;

  public VmReadinessChecker(
      CommandRunner commandRunner, int readinessAttempts, Duration readinessSleep) {
    this.commandRunner = commandRunner;
    this.readinessAttempts = readinessAttempts;
    this.readinessSleep = readinessSleep;
  }

  public ProcessResult waitForSsh() throws IOException, InterruptedException {
    return waitForSuccessfulCommand(READINESS_COMMAND_TIMEOUT, sshReadyCommand());
  }

  public ProcessResult waitForCloudInit() throws IOException, InterruptedException {
    return commandRunner.run(CLOUD_INIT_TIMEOUT, cloudInitReadyCommand());
  }

  public ProcessResult waitForRemoteDocker() throws IOException, InterruptedException {
    return waitForSuccessfulCommand(READINESS_COMMAND_TIMEOUT, remoteDockerReadyCommand());
  }

  public String remoteDockerReachability() {
    try {
      var result = commandRunner.run(READINESS_COMMAND_TIMEOUT, remoteDockerReadyCommand());
      if (result.successful()) {
        return "yes";
      }

      return "no (" + QemuCommandFailure.reason(result) + ")";
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "no (status check was interrupted)";
    } catch (IOException e) {
      return "no (" + e.getMessage() + ")";
    }
  }

  public String sshCommandText() {
    return SSH + " -p " + SSH_PORT + " " + SSH_TARGET;
  }

  private ProcessResult waitForSuccessfulCommand(Duration timeout, List<String> command)
      throws IOException, InterruptedException {
    var lastResult = new ProcessResult(1, "", "Command was not attempted.");

    for (int attempt = 0; attempt < readinessAttempts; attempt++) {
      lastResult = commandRunner.run(timeout, command);
      if (lastResult.successful()) {
        return lastResult;
      }

      if (attempt < readinessAttempts - 1 && !readinessSleep.isZero()) {
        Thread.sleep(readinessSleep.toMillis());
      }
    }

    return lastResult;
  }

  private List<String> sshReadyCommand() {
    return sshCommand("true");
  }

  private List<String> cloudInitReadyCommand() {
    return sshCommand("cloud-init", "status", "--wait");
  }

  private List<String> remoteDockerReadyCommand() {
    return sshCommand("docker", "info");
  }

  private List<String> sshCommand(String... remoteCommand) {
    var command =
        new ArrayList<>(
            List.of(
                SSH,
                "-p",
                SSH_PORT,
                "-o",
                "BatchMode=yes",
                "-o",
                "StrictHostKeyChecking=no",
                "-o",
                "UserKnownHostsFile=/dev/null",
                SSH_TARGET));

    command.addAll(List.of(remoteCommand));
    return List.copyOf(command);
  }
}
