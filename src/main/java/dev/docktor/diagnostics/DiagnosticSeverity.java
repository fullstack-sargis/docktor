package dev.docktor.diagnostics;

public enum DiagnosticSeverity {
  OK("✅"),
  WARNING("⚠️"),
  ERROR("❌");

  private final String symbol;

  DiagnosticSeverity(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }
}
