package dev.docktor.core;

import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.IOException;
import java.util.List;

public class ToolChecker {
  private final CommandRunner commandRunner;

  public ToolChecker(CommandRunner commandRunner) {
    this.commandRunner = commandRunner;
  }

  public DiagnosticMessage checkTool(ToolRequirement requirement) {
    for (var executable : requirement.executables()) {
      if (isExecutableAvailable(executable)) {
        return DiagnosticMessage.ok(requirement.displayName() + " found");
      }
    }

    return DiagnosticMessage.error(
        requirement.displayName() + " not found",
        "Required CLI is missing from PATH.",
        requirement.installInstruction());
  }

  private boolean isExecutableAvailable(String executable) {
    try {
      return commandRunner.run("which", executable).successful();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    } catch (IOException e) {
      return false;
    }
  }

  public record ToolRequirement(
      String displayName, List<String> executables, String installInstruction) {
    public static ToolRequirement single(
        String displayName, String executable, String installInstruction) {
      return new ToolRequirement(displayName, List.of(executable), installInstruction);
    }
  }
}
