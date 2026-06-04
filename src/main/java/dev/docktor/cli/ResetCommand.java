package dev.docktor.cli;

import dev.docktor.qemu.QemuManager;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "reset", description = "Stop and delete the Docktor QEMU VM state.")
public class ResetCommand implements Callable<Integer> {
  private final QemuManager qemuManager;

  @Spec private CommandSpec spec;

  public ResetCommand(QemuManager qemuManager) {
    this.qemuManager = qemuManager;
  }

  @Override
  public Integer call() {
    OutputPrinter.print(spec.commandLine().getOut(), List.of(qemuManager.reset()));
    return 0;
  }
}
