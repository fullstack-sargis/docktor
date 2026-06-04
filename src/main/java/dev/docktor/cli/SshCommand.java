package dev.docktor.cli;

import dev.docktor.qemu.QemuManager;
import java.util.List;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "ssh", description = "Open a shell in the Docktor VM.")
public class SshCommand implements Callable<Integer> {
  private final QemuManager qemuManager;

  @Spec private CommandSpec spec;

  public SshCommand(QemuManager qemuManager) {
    this.qemuManager = qemuManager;
  }

  @Override
  public Integer call() {
    OutputPrinter.print(spec.commandLine().getOut(), List.of(qemuManager.ssh()));
    return 0;
  }
}
