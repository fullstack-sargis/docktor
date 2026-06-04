package dev.docktor.docker;

import static org.assertj.core.api.Assertions.assertThat;

import dev.docktor.core.FakeCommandRunner;
import dev.docktor.core.ProcessResult;
import dev.docktor.diagnostics.DiagnosticSeverity;
import java.util.List;
import org.junit.jupiter.api.Test;

class DockerContextManagerTest {
  @Test
  void createsDockerContextWhenMissing() {
    var runner = new FakeCommandRunner();
    runner.returns(inspectCommand(), new ProcessResult(1, "", "not found"));
    runner.returns(createCommand(), new ProcessResult(0, "created", ""));
    runner.returns(useCommand(), new ProcessResult(0, "docktor", ""));

    var result = new DockerContextManager(runner).configureDocktorContext();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.OK);
    assertThat(result.message()).isEqualTo("Docker context is set to docktor");
    assertThat(runner.commands()).containsSequence(inspectCommand(), createCommand(), useCommand());
  }

  @Test
  void existingDockerContextDoesNotFail() {
    var runner = new FakeCommandRunner();
    runner.returns(inspectCommand(), new ProcessResult(0, "[]", ""));
    runner.returns(removeCommand(), new ProcessResult(0, "removed", ""));
    runner.returns(createCommand(), new ProcessResult(0, "created", ""));
    runner.returns(useCommand(), new ProcessResult(0, "docktor", ""));

    var result = new DockerContextManager(runner).configureDocktorContext();

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.OK);
    assertThat(runner.commands())
        .containsSequence(inspectCommand(), removeCommand(), createCommand(), useCommand());
  }

  @Test
  void switchesToDocktorContext() {
    var runner = new FakeCommandRunner();
    runner.returns(inspectCommand(), new ProcessResult(0, "[]", ""));
    runner.returns(removeCommand(), new ProcessResult(0, "removed", ""));
    runner.returns(createCommand(), new ProcessResult(0, "created", ""));
    runner.returns(useCommand(), new ProcessResult(0, "docktor", ""));

    new DockerContextManager(runner).configureDocktorContext();

    assertThat(runner.commands()).contains(useCommand());
  }

  private List<String> inspectCommand() {
    return List.of("docker", "context", "inspect", "docktor");
  }

  private List<String> createCommand() {
    return List.of("docker", "context", "create", "docktor", "--docker", "host=ssh://docktor-vm");
  }

  private List<String> removeCommand() {
    return List.of("docker", "context", "rm", "-f", "docktor");
  }

  private List<String> useCommand() {
    return List.of("docker", "context", "use", "docktor");
  }
}
