package dev.docktor.qemu;

import dev.docktor.core.ProcessResult;

public final class QemuCommandFailure {
  private QemuCommandFailure() {}

  public static String reason(ProcessResult result) {
    if (result.stderr() != null && !result.stderr().isBlank()) {
      return result.stderr();
    }

    if (result.stdout() != null && !result.stdout().isBlank()) {
      return result.stdout();
    }

    return "Command exited with code " + result.exitCode() + ".";
  }
}
