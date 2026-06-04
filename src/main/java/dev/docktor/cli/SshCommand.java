package dev.docktor.cli;

import dev.docktor.lima.LimaManager;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "ssh", description = "Open a shell in the Docktor VM. TODO implementation.")
public class SshCommand implements Callable<Integer> {
  private final LimaManager limaManager;

  @Spec private CommandSpec spec;

  public SshCommand(LimaManager limaManager) {
    this.limaManager = limaManager;
  }

  @Override
  public Integer call() {
    var message = limaManager.ssh();
    spec.commandLine().getOut().println(message.symbol() + " " + message.message());
    return 0;
  }
}
