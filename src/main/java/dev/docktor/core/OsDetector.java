package dev.docktor.core;

public class OsDetector {

  public String currentOsName() {
    return System.getProperty("os.name", "unknown");
  }

  public boolean isMacOs() {
    return currentOsName().toLowerCase().contains("mac");
  }
}
