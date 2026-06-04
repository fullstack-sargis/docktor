package dev.docktor.core;

import java.io.IOException;
import java.util.List;

public interface CommandRunner {

  ProcessResult run(List<String> command) throws IOException, InterruptedException;

  default ProcessResult run(String... command) throws IOException, InterruptedException {
    return run(List.of(command));
  }
}
