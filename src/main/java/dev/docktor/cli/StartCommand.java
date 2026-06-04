package dev.docktor.cli;

import dev.docktor.diagnostics.DiagnosticSeverity;
import dev.docktor.progress.ConsoleProgressReporter;
import dev.docktor.qemu.QemuManager;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "start", description = "Create and start the Docktor QEMU VM.")
public class StartCommand implements Callable<Integer> {
  private final QemuManager qemuManager;

  @Spec private CommandSpec spec;

  public StartCommand(QemuManager qemuManager) {
    this.qemuManager = qemuManager;
  }

  @Override
  public Integer call() {
    var out = spec.commandLine().getOut();
    var message = qemuManager.withProgressReporter(new ConsoleProgressReporter(out)).start();

    OutputPrinter.print(out, List.of(message));

    return message.severity() == DiagnosticSeverity.ERROR ? 1 : 0;
  }
}
