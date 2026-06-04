package dev.docktor.progress;

public class NoOpProgressReporter implements ProgressReporter {
  @Override
  public void report(String message) {
    // Intentionally empty.
  }
}
