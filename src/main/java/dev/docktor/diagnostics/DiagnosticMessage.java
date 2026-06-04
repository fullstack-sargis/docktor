package dev.docktor.diagnostics;

import java.util.Optional;

public record DiagnosticMessage(
    DiagnosticSeverity severity, String message, String rootCause, String suggestedFix) {
  public static DiagnosticMessage ok(String message) {
    return new DiagnosticMessage(DiagnosticSeverity.OK, message, null, null);
  }

  public static DiagnosticMessage warning(String message, String rootCause, String suggestedFix) {
    return new DiagnosticMessage(DiagnosticSeverity.WARNING, message, rootCause, suggestedFix);
  }

  public static DiagnosticMessage error(String message, String rootCause, String suggestedFix) {
    return new DiagnosticMessage(DiagnosticSeverity.ERROR, message, rootCause, suggestedFix);
  }

  public String symbol() {
    return severity.symbol();
  }

  public Optional<String> rootCauseOptional() {
    return Optional.ofNullable(rootCause);
  }

  public Optional<String> suggestedFixOptional() {
    return Optional.ofNullable(suggestedFix);
  }
}
