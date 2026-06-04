package dev.docktor.progress;

import java.io.PrintWriter;

public class ConsoleProgressReporter implements ProgressReporter {
  private static final String PROGRESS_SYMBOL = "⏳";

  private final PrintWriter out;

  public ConsoleProgressReporter(PrintWriter out) {
    this.out = out;
  }

  @Override
  public void report(String message) {
    out.printf(" %s %s%n", PROGRESS_SYMBOL, message);
    out.flush();
  }
}
