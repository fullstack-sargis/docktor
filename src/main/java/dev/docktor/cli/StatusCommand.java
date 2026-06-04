package dev.docktor.cli;

import dev.docktor.diagnostics.DiagnosticMessage;
import dev.docktor.docker.DockerContextManager;
import dev.docktor.docker.DockerManager;
import dev.docktor.qemu.QemuManager;
import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "status", description = "Show Docktor VM and Docker status.")
public class StatusCommand implements Callable<Integer> {
  private final QemuManager qemuManager;
  private final DockerManager dockerManager;
  private final DockerContextManager dockerContextManager;

  @Spec private CommandSpec spec;

  public StatusCommand(
      QemuManager qemuManager,
      DockerManager dockerManager,
      DockerContextManager dockerContextManager) {
    this.qemuManager = qemuManager;
    this.dockerManager = dockerManager;
    this.dockerContextManager = dockerContextManager;
  }

  @Override
  public Integer call() {
    var out = spec.commandLine().getOut();

    print(out, qemuManager.status());
    print(out, dockerManager.dockerDaemonReachable());
    print(out, dockerContextManager.currentContext());

    return 0;
  }

  private void print(java.io.PrintWriter out, DiagnosticMessage message) {
    out.printf(" %s %s%n", message.symbol(), message.message());
  }
}
