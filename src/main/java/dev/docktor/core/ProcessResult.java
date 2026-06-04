package dev.docktor.core;

public record ProcessResult(int exitCode, String stdout, String stderr) {

  public boolean successful() {
    return exitCode == 0;
  }
}
