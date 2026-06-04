package dev.docktor.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessCommandRunner implements CommandRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCommandRunner.class);
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration DEFAULT_INTERACTIVE_TIMEOUT = Duration.ofHours(1);
  private static final int TIMEOUT_EXIT_CODE = 124;

  @Override
  public ProcessResult run(List<String> command) throws IOException, InterruptedException {
    return run(DEFAULT_TIMEOUT, command);
  }

  @Override
  public ProcessResult run(Duration timeout, List<String> command)
      throws IOException, InterruptedException {
    LOGGER.debug("Running command: {}", command);

    var process = new ProcessBuilder(command).start();

    var stdout = readAsync(process.getInputStream());
    var stderr = readAsync(process.getErrorStream());

    boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);

    if (!finished) {
      process.destroyForcibly();

      return new ProcessResult(
          TIMEOUT_EXIT_CODE,
          join(stdout).trim(),
          "Command timed out after " + timeout.toSeconds() + " seconds");
    }

    int exitCode = process.exitValue();

    return new ProcessResult(exitCode, join(stdout).trim(), join(stderr).trim());
  }

  @Override
  public ProcessResult runInteractive(List<String> command)
      throws IOException, InterruptedException {
    LOGGER.debug("Running interactive command: {}", command);

    var process = new ProcessBuilder(command).inheritIO().start();

    boolean finished =
        process.waitFor(DEFAULT_INTERACTIVE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

    if (!finished) {
      process.destroyForcibly();

      return new ProcessResult(
          TIMEOUT_EXIT_CODE,
          "",
          "Command timed out after " + DEFAULT_INTERACTIVE_TIMEOUT.toSeconds() + " seconds");
    }

    return new ProcessResult(process.exitValue(), "", "");
  }

  private CompletableFuture<String> readAsync(InputStream inputStream) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new CommandOutputReadException(e);
          }
        });
  }

  private String join(CompletableFuture<String> output) throws IOException, InterruptedException {
    try {
      return output.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw e;
    } catch (ExecutionException e) {
      if (e.getCause() instanceof CommandOutputReadException readException) {
        throw readException.ioException;
      }
      throw new IOException("Failed to read process output", e);
    }
  }

  private static class CommandOutputReadException extends RuntimeException {
    private final IOException ioException;

    private CommandOutputReadException(IOException ioException) {
      super(ioException);
      this.ioException = ioException;
    }
  }
}
