package dev.docktor.diagnostics;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class PortCheckerTest {
  @Test
  void detectsReachableLocalPort() throws IOException {
    try (var serverSocket = new ServerSocket(0)) {
      boolean reachable =
          new PortChecker()
              .canConnect("localhost", serverSocket.getLocalPort(), Duration.ofMillis(500));

      assertThat(reachable).isTrue();
    }
  }

  @Test
  void detectsClosedLocalPort() {
    var reachable = new PortChecker().canConnect("localhost", 1, Duration.ofMillis(50));

    assertThat(reachable).isFalse();
  }
}
