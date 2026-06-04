package dev.docktor.diagnostics;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.core.OsDetector;
import dev.docktor.core.ToolChecker;
import dev.docktor.core.ToolChecker.ToolRequirement;
import dev.docktor.docker.DockerManager;
import java.util.ArrayList;
import java.util.List;

public class DoctorService {
  private final OsDetector osDetector;
  private final ArchitectureDetector architectureDetector;
  private final ToolChecker toolChecker;
  private final DockerManager dockerManager;

  public DoctorService(
      OsDetector osDetector,
      ArchitectureDetector architectureDetector,
      ToolChecker toolChecker,
      DockerManager dockerManager,
      PortChecker portChecker) {
    this.osDetector = osDetector;
    this.architectureDetector = architectureDetector;
    this.toolChecker = toolChecker;
    this.dockerManager = dockerManager;
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
        toolChecker.checkTool(ToolRequirement.single("Lima", "limactl", "brew install lima")),
        toolChecker.checkTool(
            new ToolRequirement(
                "QEMU", List.of("qemu-system-aarch64", "qemu-system-x86_64"), "brew install qemu")),
        toolChecker.checkTool(
            ToolRequirement.single("Docker CLI", "docker", "brew install --cask docker")));
  }

  private DiagnosticMessage checkMacOs() {
    if (!osDetector.isMacOs()) {
      return DiagnosticMessage.warning(
          "macOS not detected: " + osDetector.currentOsName(),
          "Docktor's first milestone is designed for macOS hosts.",
          "Run Docktor on macOS or add Linux host support later.");
    }
    return DiagnosticMessage.ok("macOS detected: " + architectureDetector.currentArchitecture());
  }
}
