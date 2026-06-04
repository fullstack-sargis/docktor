package dev.docktor.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocktorApplicationTest {

  @Test
  void registersNestedDiagnosticCommands() {
    var commandLine = DocktorApplication.createCommandLine();

    assertThat(commandLine.getSubcommands())
        .containsOnlyKeys("doctor", "status", "start", "stop", "ssh", "docker");
    assertThat(commandLine.getSubcommands().get("docker").getSubcommands()).containsKey("check");
  }
}
