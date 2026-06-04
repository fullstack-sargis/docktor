package dev.docktor.core;

import java.util.Locale;

public class ArchitectureDetector {

  public String currentArchitecture() {
    return normalize(System.getProperty("os.arch", "unknown"));
  }

  public String normalize(String architecture) {
    if (architecture == null || architecture.isBlank()) {
      throw new IllegalArgumentException("Architecture must not be blank");
    }

    var normalized = architecture.toLowerCase(Locale.ROOT);

    if (normalized.equals("aarch64") || normalized.equals("arm64")) {
      return "arm64";
    }

    if (normalized.equals("x86_64") || normalized.equals("amd64")) {
      return "x86_64";
    }

    throw new IllegalArgumentException("Unsupported architecture: " + architecture);
  }
}
