package dev.docktor.cli;

import dev.docktor.diagnostics.DoctorService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

@Command(name = "doctor", description = "Run all Docktor diagnostics.")
public class DoctorCommand implements Callable<Integer> {
    private final DoctorService doctorService;

    @Spec
    private CommandSpec spec;

    public DoctorCommand(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @Override
    public Integer call() {
        OutputPrinter.print(spec.commandLine().getOut(), doctorService.runDoctor());
        return 0;
    }
}
