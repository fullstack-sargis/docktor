package dev.docktor.cli;

import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.diagnostics.DiagnosticSeverity;
import java.io.PrintWriter;
import java.util.List;

final class OutputPrinter {

  private OutputPrinter() {}

  static void print(PrintWriter out, List<DiagnosticMessage> messages) {
    for (DiagnosticMessage message : messages) {
      printMessage(out, message);
      message
          .rootCauseOptional()
          .ifPresent(rootCause -> out.printf("Probable root cause: %s%n", rootCause));

      message
          .suggestedFixOptional()
          .ifPresent(suggestedFix -> out.printf("Suggested fix: %s%n", suggestedFix));
    }
  }

  private static void printMessage(PrintWriter out, DiagnosticMessage message) {
    var lines = message.message().split("\\R", -1);
    for (String line : lines) {
      if (line.isEmpty()) {
        out.println();
      } else if (message.severity() == DiagnosticSeverity.OK
          && line.startsWith("Docker daemon may still")) {
        out.printf(" %s %s%n", "⏳", line);
      } else if (line.startsWith("  ") || line.equals("Try:")) {
        out.printf("%s%n", line);
      } else {
        out.printf(" %s %s%n", message.symbol(), line);
      }
    }
  }
}
