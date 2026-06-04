package dev.docktor.core;

import static org.assertj.core.api.Assertions.assertThat;

import dev.docktor.core.ToolChecker.ToolRequirement;
import dev.docktor.diagnostics.DiagnosticSeverity;
import java.util.List;
import org.junit.jupiter.api.Test;

class ToolCheckerTest {
  @Test
  void reportsToolFoundWhenExecutableIsOnPath() {
    var runner = new FakeCommandRunner();
    runner.returns(List.of("which", "docker"), new ProcessResult(0, "/usr/local/bin/docker", ""));

    var result =
        new ToolChecker(runner)
            .checkTool(
                ToolRequirement.single("Docker CLI", "docker", "brew install --cask docker"));

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.OK);
    assertThat(result.message()).isEqualTo("Docker CLI found");
  }

  @Test
  void reportsSuggestedInstallWhenToolIsMissing() {
    var runner = new FakeCommandRunner();

    var result =
        new ToolChecker(runner)
            .checkTool(ToolRequirement.single("Lima", "limactl", "brew install lima"));

    assertThat(result.severity()).isEqualTo(DiagnosticSeverity.ERROR);
    assertThat(result.message()).isEqualTo("Lima not found");
    assertThat(result.suggestedFixOptional()).contains("brew install lima");
  }
}
