package dev.docktor.qemu;

import static org.assertj.core.api.Assertions.assertThat;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.core.FakeCommandRunner;
import dev.docktor.core.ProcessResult;
import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.diagnostics.DiagnosticSeverity;
import dev.docktor.docker.DockerContextManager;
import dev.docktor.progress.ProgressReporter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QemuManagerTest {
  @TempDir Path tempDir;

  @Test
  void reportsMissingQemuWithBrewInstallSuggestion() {
    var runner = new FakeCommandRunner();
    runner.returns(List.of("which", "qemu-system-aarch64"), new ProcessResult(1, "", "missing"));

    var result =
        new QemuManager(runner, new QemuConfigGenerator(new FixedArchitecture("arm64"))).start();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.WARNING);
    assertThat(result.message()).isEqualTo("QEMU is not installed");
    assertThat(result.suggestedFixOptional()).contains("brew install qemu");
  }

  @Test
  void reportsMissingHdiutilWithMacosSuggestion() {
    var runner = new FakeCommandRunner();
    runner.returns(
        List.of("which", "qemu-system-aarch64"),
        new ProcessResult(0, "/opt/homebrew/bin/qemu-system-aarch64", ""));
    runner.returns(
        List.of("which", "qemu-img"), new ProcessResult(0, "/opt/homebrew/bin/qemu-img", ""));
    runner.returns(List.of("which", "hdiutil"), new ProcessResult(1, "", "missing"));

    var result =
        new QemuManager(runner, new QemuConfigGenerator(new FixedArchitecture("arm64"))).start();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.WARNING);
    assertThat(result.message()).isEqualTo("hdiutil is not available");
    assertThat(result.suggestedFixOptional())
        .contains("Run Docktor on macOS or ensure hdiutil is available on PATH.");
  }

  @Test
  void checksArchitectureSpecificQemuBinary() {
    var runner = new FakeCommandRunner();
    runner.returns(List.of("which", "qemu-system-x86_64"), new ProcessResult(1, "", "missing"));

    var result =
        new QemuManager(runner, new QemuConfigGenerator(new FixedArchitecture("x86_64"))).start();

    assertThat(result.message()).isEqualTo("QEMU is not installed");
    assertThat(result.rootCauseOptional())
        .contains("Required command qemu-system-x86_64 was not found on PATH.");
  }

  @Test
  void startReturnsWarningIfVmStartsButDockerReadinessFails() throws Exception {
    var runner = new FakeCommandRunner();
    var config = new TestQemuConfigGenerator(tempDir);
    Files.createFile(config.imagePath());
    Files.createFile(config.diskPath());
    runner.returns(List.of("which", "qemu-system-aarch64"), new ProcessResult(0, "/bin/qemu", ""));
    runner.returns(List.of("which", "qemu-img"), new ProcessResult(0, "/bin/qemu-img", ""));
    runner.returns(List.of("which", "hdiutil"), new ProcessResult(0, "/usr/bin/hdiutil", ""));
    runner.returns(
        List.of(
            "hdiutil",
            "makehybrid",
            "-o",
            config.seedIsoPath().toString(),
            config.seedDirectory().toString(),
            "-iso",
            "-joliet",
            "-default-volume-name",
            "cidata"),
        new ProcessResult(0, "", ""));
    runner.returns(sshReadyCommand(), new ProcessResult(0, "", ""));
    runner.returns(cloudInitReadyCommand(), new ProcessResult(0, "done", ""));
    runner.returns(remoteDockerReadyCommand(), new ProcessResult(1, "", "docker unavailable"));

    var result =
        new QemuManager(runner, config, new DockerContextManager(runner), 1, Duration.ZERO).start();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.WARNING);
    assertThat(result.message()).isEqualTo("QEMU VM started, but Docker is not ready");
    assertThat(result.rootCauseOptional()).contains("docker unavailable");
  }

  @Test
  void startReportsProgressAndReturnsMultilineSuccess() throws Exception {
    var originalUserHome = System.getProperty("user.home");
    var home = tempDir.resolve("home");
    Files.createDirectories(home.resolve(".ssh"));
    Files.writeString(home.resolve(".ssh").resolve("id_rsa.pub"), "ssh-rsa test-key");
    System.setProperty("user.home", home.toString());

    try {
      var runner = new FakeCommandRunner();
      var config = new TestQemuConfigGenerator(tempDir.resolve("vm"));
      Files.createDirectories(config.workingDir());
      Files.createFile(config.imagePath());
      Files.createFile(config.diskPath());
      runner.returns(
          List.of("which", "qemu-system-aarch64"), new ProcessResult(0, "/bin/qemu", ""));
      runner.returns(List.of("which", "qemu-img"), new ProcessResult(0, "/bin/qemu-img", ""));
      runner.returns(List.of("which", "hdiutil"), new ProcessResult(0, "/usr/bin/hdiutil", ""));
      runner.returns(seedIsoCommand(config), new ProcessResult(0, "", ""));
      runner.returns(sshReadyCommand(), new ProcessResult(0, "", ""));
      runner.returns(cloudInitReadyCommand(), new ProcessResult(0, "done", ""));
      runner.returns(remoteDockerReadyCommand(), new ProcessResult(0, "ok", ""));
      var progress = new RecordingProgressReporter();

      var result =
          new QemuManager(
                  runner,
                  config,
                  new SuccessfulDockerContextManager(runner),
                  1,
                  Duration.ZERO,
                  progress)
              .start();

      assertThat(progress.messages())
          .containsExactly(
              "Preparing VM storage...",
              "Preparing cloud-init seed...",
              "Starting QEMU VM...",
              "Waiting for SSH...",
              "Cloud-init is running. Ubuntu is installing packages and configuring Docker. This may take several minutes on first boot...",
              "Waiting for Docker inside VM...",
              "Configuring Docker context...");
      assertThat(result.severity()).isEqualTo(DiagnosticSeverity.OK);
      assertThat(result.message())
          .isEqualTo(
              """
              QEMU VM started.
              Docker context is set to docktor.
              Docker daemon may still be starting inside the VM.
              Try:
                docktor status
                docker run hello-world""");
    } finally {
      System.setProperty("user.home", originalUserHome);
    }
  }

  @Test
  void statusReportsDockerSshAndContextInfo() throws Exception {
    var runner = new FakeCommandRunner();
    var config = new TestQemuConfigGenerator(tempDir);
    Files.createDirectories(config.workingDir());
    Files.writeString(config.workingDir().resolve("docktor-qemu.pid"), "12345");
    runner.returns(List.of("kill", "-0", "12345"), new ProcessResult(0, "", ""));
    runner.returns(remoteDockerReadyCommand(), new ProcessResult(0, "ok", ""));

    var result = new QemuManager(runner, config).status();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.OK);
    assertThat(result.message()).contains("QEMU VM PID: 12345");
    assertThat(result.message()).contains("SSH command: ssh -p 2222 docktor@127.0.0.1");
    assertThat(result.message()).contains("Docker context: docktor");
    assertThat(result.message()).contains("Remote Docker reachable: yes");
  }

  private static List<String> sshReadyCommand() {
    return List.of(
        "ssh",
        "-p",
        "2222",
        "-o",
        "BatchMode=yes",
        "-o",
        "StrictHostKeyChecking=no",
        "-o",
        "UserKnownHostsFile=/dev/null",
        "docktor@127.0.0.1",
        "true");
  }

  private static List<String> cloudInitReadyCommand() {
    return List.of(
        "ssh",
        "-p",
        "2222",
        "-o",
        "BatchMode=yes",
        "-o",
        "StrictHostKeyChecking=no",
        "-o",
        "UserKnownHostsFile=/dev/null",
        "docktor@127.0.0.1",
        "cloud-init",
        "status",
        "--wait");
  }

  private static List<String> remoteDockerReadyCommand() {
    return List.of(
        "ssh",
        "-p",
        "2222",
        "-o",
        "BatchMode=yes",
        "-o",
        "StrictHostKeyChecking=no",
        "-o",
        "UserKnownHostsFile=/dev/null",
        "docktor@127.0.0.1",
        "docker",
        "info");
  }

  private static List<String> seedIsoCommand(QemuConfigGenerator config) {
    return List.of(
        "hdiutil",
        "makehybrid",
        "-o",
        config.seedIsoPath().toString(),
        config.seedDirectory().toString(),
        "-iso",
        "-joliet",
        "-default-volume-name",
        "cidata");
  }

  private static class FixedArchitecture extends ArchitectureDetector {
    private final String architecture;

    private FixedArchitecture(String architecture) {
      this.architecture = architecture;
    }

    @Override
    public String currentArchitecture() {
      return architecture;
    }
  }

  private static class TestQemuConfigGenerator extends QemuConfigGenerator {
    private final Path workingDir;

    private TestQemuConfigGenerator(Path workingDir) {
      super(new FixedArchitecture("arm64"));
      this.workingDir = workingDir;
    }

    @Override
    public Path workingDir() {
      return workingDir;
    }

    @Override
    public List<String> startCommand() {
      return List.of("true");
    }
  }

  private static class RecordingProgressReporter implements ProgressReporter {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void report(String message) {
      messages.add(message);
    }

    private List<String> messages() {
      return messages;
    }
  }

  private static class SuccessfulDockerContextManager extends DockerContextManager {
    private SuccessfulDockerContextManager(FakeCommandRunner runner) {
      super(runner);
    }

    @Override
    public DiagnosticMessage configureDocktorContext() {
      return DiagnosticMessage.ok("Docker context is set to docktor");
    }
  }
}
