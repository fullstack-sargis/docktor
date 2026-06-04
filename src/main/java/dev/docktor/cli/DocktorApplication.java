package dev.docktor.cli;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.core.OsDetector;
import dev.docktor.core.ProcessCommandRunner;
import dev.docktor.core.ToolChecker;
import dev.docktor.diagnostics.DoctorService;
import dev.docktor.diagnostics.PortChecker;
import dev.docktor.docker.DockerContextManager;
import dev.docktor.docker.DockerManager;
import dev.docktor.qemu.QemuConfigGenerator;
import dev.docktor.qemu.QemuManager;
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

    var qemuConfigGenerator = new QemuConfigGenerator(architectureDetector);

    var qemuManager = new QemuManager(commandRunner, qemuConfigGenerator, dockerContextManager);

    var doctorService =
        new DoctorService(
            osDetector,
            architectureDetector,
            toolChecker,
            dockerManager,
            portChecker,
            qemuConfigGenerator);

    var root = new DocktorCommand();
    var commandLine = new CommandLine(root);

    commandLine.addSubcommand("doctor", new DoctorCommand(doctorService));
    commandLine.addSubcommand(
        "status", new StatusCommand(qemuManager, dockerManager, dockerContextManager));
    commandLine.addSubcommand("start", new StartCommand(qemuManager));
    commandLine.addSubcommand("stop", new StopCommand(qemuManager));
    commandLine.addSubcommand("ssh", new SshCommand(qemuManager));
    commandLine.addSubcommand("reset", new ResetCommand(qemuManager));

    var qemu = new CommandLine(new QemuCommand());
    qemu.addSubcommand(
        "status", new StatusCommand(qemuManager, dockerManager, dockerContextManager));
    qemu.addSubcommand("start", new StartCommand(qemuManager));
    qemu.addSubcommand("stop", new StopCommand(qemuManager));
    qemu.addSubcommand("ssh", new SshCommand(qemuManager));
    qemu.addSubcommand("reset", new ResetCommand(qemuManager));
    commandLine.addSubcommand("qemu", qemu);

    var docker = new CommandLine(new DockerCommand());
    docker.addSubcommand("check", new DockerCheckCommand(doctorService));
    docker.addSubcommand("context", new DockerContextCommand(dockerContextManager));
    commandLine.addSubcommand("docker", docker);

    return commandLine;
  }
}
