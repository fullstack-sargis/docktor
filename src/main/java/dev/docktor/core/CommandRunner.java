package dev.docktor.core;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public interface CommandRunner {

  ProcessResult run(List<String> command) throws IOException, InterruptedException;

  default ProcessResult run(Duration timeout, List<String> command)
      throws IOException, InterruptedException {
    return run(command);
  }

  ProcessResult runInteractive(List<String> command) throws IOException, InterruptedException;

  default ProcessResult run(String... command) throws IOException, InterruptedException {
    return run(List.of(command));
  }

  default ProcessResult run(Duration timeout, String... command)
      throws IOException, InterruptedException {
    return run(timeout, List.of(command));
  }
}
