package dev.docktor.lima;

import static org.assertj.core.api.Assertions.assertThat;

import dev.docktor.core.FakeCommandRunner;
import dev.docktor.core.ProcessResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class LimaManagerTest {

  @Test
  void statusUsesLimactlListForDocktorVm() {
    var runner = new FakeCommandRunner();
    runner.returns(
        List.of("limactl", "list", "docktor"), new ProcessResult(0, "docktor Running", ""));

    var result = new LimaManager(runner, new LimaConfigGenerator()).status();

    assertThat(result.message()).contains("docktor Running");
    assertThat(runner.commands()).containsExactly(List.of("limactl", "list", "docktor"));
  }
}
