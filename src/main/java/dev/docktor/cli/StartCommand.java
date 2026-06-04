package dev.docktor.cli;

import dev.docktor.lima.LimaManager;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "start", description = "Create and start the Docktor VM. TODO implementation.")
public class StartCommand implements Callable<Integer> {
  private final LimaManager limaManager;

  @Spec private CommandSpec spec;

  public StartCommand(LimaManager limaManager) {
    this.limaManager = limaManager;
  }

  @Override
  public Integer call() {
    var message = limaManager.start();
    spec.commandLine().getOut().println(message.symbol() + " " + message.message());
    return 0;
  }
}
