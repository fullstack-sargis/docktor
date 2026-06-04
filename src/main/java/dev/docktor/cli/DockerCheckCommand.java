package dev.docktor.cli;

import dev.docktor.diagnostics.DoctorService;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "check", description = "Check Docker CLI and daemon reachability.")
public class DockerCheckCommand implements Callable<Integer> {

  private final DoctorService doctorService;

  @Spec private CommandSpec spec;

  public DockerCheckCommand(DoctorService doctorService) {
    this.doctorService = doctorService;
  }

  @Override
  public Integer call() {
    OutputPrinter.print(spec.commandLine().getOut(), doctorService.checkDocker());
    return 0;
  }
}
