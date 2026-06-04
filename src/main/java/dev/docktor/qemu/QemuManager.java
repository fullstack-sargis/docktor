package dev.docktor.qemu;

import dev.docktor.core.CommandRunner;
import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.diagnostics.DiagnosticSeverity;
import dev.docktor.docker.DockerContextManager;
import dev.docktor.progress.NoOpProgressReporter;
import dev.docktor.progress.ProgressReporter;
import dev.docktor.qemu.checks.QemuPrerequisitesChecker;
import dev.docktor.qemu.cloudinit.CloudInitManager;
import dev.docktor.qemu.runtime.QemuProcessManager;
import dev.docktor.qemu.runtime.VmReadinessChecker;
import dev.docktor.qemu.storage.QemuStorageManager;
import java.io.IOException;
import java.time.Duration;

public class QemuManager {
  private static final int DEFAULT_READINESS_ATTEMPTS = 60;
  private static final Duration DEFAULT_READINESS_SLEEP = Duration.ofSeconds(2);

  private final DockerContextManager dockerContextManager;
  private final QemuPrerequisitesChecker prerequisitesChecker;
  private final QemuStorageManager storageManager;
  private final CloudInitManager cloudInitManager;
  private final QemuProcessManager processManager;
  private final VmReadinessChecker readinessChecker;
  private final ProgressReporter progressReporter;

  public QemuManager(CommandRunner commandRunner, QemuConfigGenerator configGenerator) {
    this(commandRunner, configGenerator, new DockerContextManager(commandRunner));
  }

  public QemuManager(
      CommandRunner commandRunner,
      QemuConfigGenerator configGenerator,
      DockerContextManager dockerContextManager) {
    this(commandRunner, configGenerator, dockerContextManager, new NoOpProgressReporter());
  }

  public QemuManager(
      CommandRunner commandRunner,
      QemuConfigGenerator configGenerator,
      DockerContextManager dockerContextManager,
      ProgressReporter progressReporter) {
    this(
        dockerContextManager,
        new QemuPrerequisitesChecker(commandRunner, configGenerator),
        new QemuStorageManager(commandRunner, configGenerator),
        new CloudInitManager(commandRunner, configGenerator),
        new QemuProcessManager(commandRunner, configGenerator),
        new VmReadinessChecker(commandRunner, DEFAULT_READINESS_ATTEMPTS, DEFAULT_READINESS_SLEEP),
        progressReporter);
  }

  QemuManager(
      CommandRunner commandRunner,
      QemuConfigGenerator configGenerator,
      DockerContextManager dockerContextManager,
      int readinessAttempts,
      Duration readinessSleep) {
    this(
        commandRunner,
        configGenerator,
        dockerContextManager,
        readinessAttempts,
        readinessSleep,
        new NoOpProgressReporter());
  }

  QemuManager(
      CommandRunner commandRunner,
      QemuConfigGenerator configGenerator,
      DockerContextManager dockerContextManager,
      int readinessAttempts,
      Duration readinessSleep,
      ProgressReporter progressReporter) {
    this(
        dockerContextManager,
        new QemuPrerequisitesChecker(commandRunner, configGenerator),
        new QemuStorageManager(commandRunner, configGenerator),
        new CloudInitManager(commandRunner, configGenerator),
        new QemuProcessManager(commandRunner, configGenerator),
        new VmReadinessChecker(commandRunner, readinessAttempts, readinessSleep),
        progressReporter);
  }

  QemuManager(
      DockerContextManager dockerContextManager,
      QemuPrerequisitesChecker prerequisitesChecker,
      QemuStorageManager storageManager,
      CloudInitManager cloudInitManager,
      QemuProcessManager processManager,
      VmReadinessChecker readinessChecker,
      ProgressReporter progressReporter) {
    this.dockerContextManager = dockerContextManager;
    this.prerequisitesChecker = prerequisitesChecker;
    this.storageManager = storageManager;
    this.cloudInitManager = cloudInitManager;
    this.processManager = processManager;
    this.readinessChecker = readinessChecker;
    this.progressReporter = progressReporter;
  }

  public QemuManager withProgressReporter(ProgressReporter progressReporter) {
    return new QemuManager(
        dockerContextManager,
        prerequisitesChecker,
        storageManager,
        cloudInitManager,
        processManager,
        readinessChecker,
        progressReporter);
  }

  public DiagnosticMessage start() {
    try {
      var prerequisitesProblem = prerequisitesChecker.check();
      if (prerequisitesProblem.isPresent()) {
        return prerequisitesProblem.get();
      }

      progressReporter.report("Preparing VM storage...");
      storageManager.prepareStorage();
      progressReporter.report("Preparing cloud-init seed...");
      cloudInitManager.prepare();
      progressReporter.report("Starting QEMU VM...");
      processManager.start();

      progressReporter.report("Waiting for SSH...");
      var sshResult = readinessChecker.waitForSsh();
      if (!sshResult.successful()) {
        return DiagnosticMessage.warning(
            "QEMU VM started, but SSH is not ready",
            QemuCommandFailure.reason(sshResult),
            "Run `docktor status` or retry `docktor start` after the VM finishes booting.");
      }

      progressReporter.report(
          "Cloud-init is running. Ubuntu is installing packages and configuring Docker. This may take several minutes on first boot...");
      var cloudInitResult = readinessChecker.waitForCloudInit();
      if (!cloudInitResult.successful()) {
        return DiagnosticMessage.warning(
            "QEMU VM started, but cloud-init is not ready",
            QemuCommandFailure.reason(cloudInitResult),
            "Run `docktor status` or retry `docktor start` after cloud-init finishes.");
      }

      progressReporter.report("Waiting for Docker inside VM...");
      var dockerResult = readinessChecker.waitForRemoteDocker();
      if (!dockerResult.successful()) {
        return DiagnosticMessage.warning(
            "QEMU VM started, but Docker is not ready",
            QemuCommandFailure.reason(dockerResult),
            "Run `docktor status` or retry `docktor docker context` after cloud-init finishes.");
      }

      progressReporter.report("Configuring Docker context...");
      var contextResult = dockerContextManager.configureDocktorContext();
      if (contextResult.severity() != DiagnosticSeverity.OK) {
        return DiagnosticMessage.warning(
            "QEMU VM started, but Docker context was not configured",
            contextResult.rootCauseOptional().orElse(contextResult.message()),
            contextResult.suggestedFixOptional().orElse("Run `docktor docker context`."));
      }

      return DiagnosticMessage.ok(
          """
          QEMU VM started.
          Docker context is set to docktor.
          Docker daemon may still be starting inside the VM.
          Try:
            docktor status
            docker run hello-world""");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return DiagnosticMessage.warning(
          "QEMU VM start was interrupted",
          "Docktor was interrupted while preparing or starting QEMU.",
          "Run `docktor status` and retry `docktor start`.");
    } catch (IOException | RuntimeException e) {
      return DiagnosticMessage.warning(
          "QEMU VM could not be started",
          e.getMessage(),
          "Check ~/.docktor/qemu/docktor-qemu.log and verify QEMU is installed.");
    }
  }

  public DiagnosticMessage stop() {
    return processManager.stop();
  }

  public DiagnosticMessage status() {
    if (!processManager.isRunning()) {
      return DiagnosticMessage.warning(
          "QEMU VM is not running",
          "PID file does not exist: " + processManager.pidPath(),
          "Run `docktor start`.");
    }

    var status =
        String.join(
            System.lineSeparator(),
            "QEMU VM PID: " + processManager.readPid(),
            "SSH command: " + readinessChecker.sshCommandText(),
            "Docker context: " + dockerContextManager.contextName(),
            "Remote Docker reachable: " + readinessChecker.remoteDockerReachability());
    return DiagnosticMessage.ok(status);
  }

  public DiagnosticMessage reset() {
    stop();

    try {
      storageManager.resetStorage();
      dockerContextManager.useDefaultContext();
      return DiagnosticMessage.ok("QEMU VM reset completed");
    } catch (IOException e) {
      return DiagnosticMessage.warning(
          "QEMU VM reset failed", e.getMessage(), "Remove ~/.docktor/qemu files manually.");
    }
  }

  public DiagnosticMessage ssh() {
    return DiagnosticMessage.ok("SSH with: " + readinessChecker.sshCommandText());
  }
}
