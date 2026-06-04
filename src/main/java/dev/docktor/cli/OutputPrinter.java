package dev.docktor.cli;

import dev.docktor.diagnostics.DiagnosticMessage;
import java.io.PrintWriter;
import java.util.List;

final class OutputPrinter {

  private OutputPrinter() {}

  static void print(PrintWriter out, List<DiagnosticMessage> messages) {
    for (DiagnosticMessage message : messages) {
      out.println(message.symbol() + " " + message.message());
      message
          .rootCauseOptional()
          .ifPresent(rootCause -> out.println("Probable root cause: " + rootCause));
      message
          .suggestedFixOptional()
          .ifPresent(suggestedFix -> out.println("Suggested fix: " + suggestedFix));
    }
  }
}
