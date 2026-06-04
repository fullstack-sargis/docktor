package dev.docktor.cli;

import dev.docktor.diagnostics.DoctorService;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "doctor", description = "Run all Docktor diagnostics.")
public class DoctorCommand implements Callable<Integer> {
  private final DoctorService doctorService;

  @Spec private CommandSpec spec;

  public DoctorCommand(DoctorService doctorService) {
    this.doctorService = doctorService;
  }

  @Override
  public Integer call() {
    OutputPrinter.print(spec.commandLine().getOut(), doctorService.runDoctor());
    return 0;
  }
}
