package dev.docktor.cli;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocktorApplicationTest {

  @Test
  void registersNestedDiagnosticCommands() {
    var commandLine = DocktorApplication.createCommandLine();

    assertThat(commandLine.getSubcommands())
        .containsOnlyKeys("doctor", "status", "start", "stop", "ssh", "reset", "qemu", "docker");
    assertThat(commandLine.getSubcommands().get("qemu").getSubcommands())
        .containsOnlyKeys("status", "start", "stop", "ssh", "reset");
    assertThat(commandLine.getSubcommands().get("docker").getSubcommands())
        .containsOnlyKeys("check", "context");
  }
}
