package dev.docktor.cli;

import dev.docktor.lima.LimaManager;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "stop", description = "Stop the Docktor VM. TODO implementation.")
public class StopCommand implements Callable<Integer> {
  private final LimaManager limaManager;

  @Spec private CommandSpec spec;

  public StopCommand(LimaManager limaManager) {
    this.limaManager = limaManager;
  }

  @Override
  public Integer call() {
    var message = limaManager.stop();
    spec.commandLine().getOut().println(message.symbol() + " " + message.message());
    return 0;
  }
}
