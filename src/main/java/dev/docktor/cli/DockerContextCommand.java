package dev.docktor.cli;

import dev.docktor.diagnostics.DiagnosticSeverity;
import dev.docktor.docker.DockerContextManager;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "context", description = "Create and switch to the Docktor Docker context.")
public class DockerContextCommand implements Callable<Integer> {
  private final DockerContextManager dockerContextManager;

  @Spec private CommandSpec spec;

  public DockerContextCommand(DockerContextManager dockerContextManager) {
    this.dockerContextManager = dockerContextManager;
  }

  @Override
  public Integer call() {
    var message = dockerContextManager.configureDocktorContext();

    OutputPrinter.print(spec.commandLine().getOut(), List.of(message));

    return message.severity() == DiagnosticSeverity.ERROR ? 1 : 0;
  }
}
