package dev.docktor.diagnostics;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.core.OsDetector;
import dev.docktor.core.ToolChecker;
import dev.docktor.core.ToolChecker.ToolRequirement;
import dev.docktor.docker.DockerManager;
import dev.docktor.qemu.QemuConfigGenerator;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {
  private final OsDetector osDetector;
  private final ArchitectureDetector architectureDetector;
  private final ToolChecker toolChecker;
  private final DockerManager dockerManager;
  private final QemuConfigGenerator qemuConfigGenerator;

  public DoctorService(
      OsDetector osDetector,
      ArchitectureDetector architectureDetector,
      ToolChecker toolChecker,
      DockerManager dockerManager,
      PortChecker portChecker,
      QemuConfigGenerator qemuConfigGenerator) {
    this.osDetector = osDetector;
    this.architectureDetector = architectureDetector;
    this.toolChecker = toolChecker;
    this.dockerManager = dockerManager;
    this.qemuConfigGenerator = qemuConfigGenerator;
  }

  public List<DiagnosticMessage> runDoctor() {
    var messages = new ArrayList<DiagnosticMessage>();
    messages.add(checkMacOs());
    messages.addAll(checkRequiredTools());
    messages.add(dockerManager.dockerDaemonReachable());
    return messages;
  }

  public List<DiagnosticMessage> checkDocker() {
    return List.of(
        toolChecker.checkTool(
            ToolRequirement.single("Docker CLI", "docker", "brew install --cask docker")),
        dockerManager.dockerDaemonReachable());
  }

  public List<DiagnosticMessage> checkRequiredTools() {
    return List.of(
        toolChecker.checkTool(
            ToolRequirement.single(
                "QEMU binary", qemuConfigGenerator.qemuBinary(), "brew install qemu")),
        toolChecker.checkTool(ToolRequirement.single("qemu-img", "qemu-img", "brew install qemu")),
        toolChecker.checkTool(
            ToolRequirement.single("Docker CLI", "docker", "brew install --cask docker")));
  }

  private DiagnosticMessage checkMacOs() {
    if (!osDetector.isMacOs()) {
      return DiagnosticMessage.warning(
          "Unsupported host OS: " + osDetector.currentOsName(),
          "The current Docktor build supports macOS hosts only.",
          "Use a macOS host.");
    }

    return DiagnosticMessage.ok(
        "Host OS supported: macOS " + architectureDetector.currentArchitecture());
  }
}
