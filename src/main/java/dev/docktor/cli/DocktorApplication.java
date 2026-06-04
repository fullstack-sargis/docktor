package dev.docktor.cli;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.core.OsDetector;
import dev.docktor.core.ProcessCommandRunner;
import dev.docktor.core.ToolChecker;
import dev.docktor.diagnostics.DoctorService;
import dev.docktor.diagnostics.PortChecker;
import dev.docktor.docker.DockerContextManager;
import dev.docktor.docker.DockerManager;
import dev.docktor.lima.LimaConfigGenerator;
import dev.docktor.lima.LimaManager;
import picocli.CommandLine;

public final class DocktorApplication {
  private DocktorApplication() {}

  public static void main(String[] args) {
    int exitCode = createCommandLine().execute(args);
    System.exit(exitCode);
  }

  public static CommandLine createCommandLine() {
    var commandRunner = new ProcessCommandRunner();
    var osDetector = new OsDetector();
    var architectureDetector = new ArchitectureDetector();
    var toolChecker = new ToolChecker(commandRunner);
    var portChecker = new PortChecker();
    var dockerManager = new DockerManager(commandRunner);
    var dockerContextManager = new DockerContextManager(commandRunner);
    var limaConfigGenerator = new LimaConfigGenerator();
    var limaManager = new LimaManager(commandRunner, limaConfigGenerator);
    var doctorService =
        new DoctorService(
            osDetector, architectureDetector, toolChecker, dockerManager, portChecker);

    var root = new DocktorCommand();
    var commandLine = new CommandLine(root);
    commandLine.addSubcommand("doctor", new DoctorCommand(doctorService));
    commandLine.addSubcommand(
        "status", new StatusCommand(limaManager, dockerManager, dockerContextManager));
    commandLine.addSubcommand("start", new StartCommand(limaManager));
    commandLine.addSubcommand("stop", new StopCommand(limaManager));
    commandLine.addSubcommand("ssh", new SshCommand(limaManager));

    var docker = new CommandLine(new DockerCommand());
    docker.addSubcommand("check", new DockerCheckCommand(doctorService));
    commandLine.addSubcommand("docker", docker);

    return commandLine;
  }
}
