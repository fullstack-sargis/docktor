package dev.docktor.diagnostics;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;

public class PortChecker {
  private static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(300);

  public boolean canConnect(String host, int port) {
    return canConnect(host, port, DEFAULT_TIMEOUT);
  }

  public boolean canConnect(String host, int port, Duration timeout) {
    try (var socket = new Socket()) {
      socket.connect(new InetSocketAddress(host, port), Math.toIntExact(timeout.toMillis()));
      return true;
    } catch (IOException | RuntimeException e) {
      return false;
    }
  }
}
