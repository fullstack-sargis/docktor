package dev.docktor.qemu;

import dev.docktor.core.ArchitectureDetector;
import dev.docktor.qemu.arch.Arm64QemuArchitectureConfig;
import dev.docktor.qemu.arch.QemuArchitectureConfig;
import dev.docktor.qemu.arch.X86_64QemuArchitectureConfig;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class QemuConfigGenerator {
  private static final String DOCKTOR_DIR = ".docktor";
  private static final String QEMU_DIR = "qemu";
  private static final String DISK_FILE_NAME = "docktor.qcow2";
  private static final String SEED_ISO_FILE_NAME = "seed.iso";
  private static final String SEED_CDR_FILE_NAME = "seed.iso.cdr";
  private static final String SEED_DIR_NAME = "seed";
  private static final String USER_DATA_FILE_NAME = "user-data";
  private static final String META_DATA_FILE_NAME = "meta-data";
  private static final String SERIAL_LOG_FILE_NAME = "serial.log";

  private static final String CPU_COUNT = "4";
  private static final String MEMORY_MB = "4096";
  private static final String SSH_PORT_FORWARD = "user,id=net0,hostfwd=tcp::2222-:22";

  private final ArchitectureDetector architectureDetector;

  public QemuConfigGenerator() {
    this(new ArchitectureDetector());
  }

  public QemuConfigGenerator(ArchitectureDetector architectureDetector) {
    this.architectureDetector = architectureDetector;
  }

  public QemuArchitectureConfig architectureConfig() {
    return architectureConfig(architectureDetector.currentArchitecture());
  }

  public QemuArchitectureConfig architectureConfig(String architecture) {
    return switch (architectureDetector.normalize(architecture)) {
      case "arm64" -> new Arm64QemuArchitectureConfig();
      case "x86_64" -> new X86_64QemuArchitectureConfig();
      default -> throw new IllegalArgumentException("Unsupported architecture: " + architecture);
    };
  }

  public Path workingDir() {
    return Path.of(System.getProperty("user.home"), DOCKTOR_DIR, QEMU_DIR);
  }

  public Path imagePath() {
    return workingDir().resolve(architectureConfig().imageFileName());
  }

  public Path diskPath() {
    return workingDir().resolve(DISK_FILE_NAME);
  }

  public Path seedIsoPath() {
    return workingDir().resolve(SEED_ISO_FILE_NAME);
  }

  public Path seedCdrPath() {
    return workingDir().resolve(SEED_CDR_FILE_NAME);
  }

  public Path seedDirectory() {
    return workingDir().resolve(SEED_DIR_NAME);
  }

  public Path userDataPath() {
    return seedDirectory().resolve(USER_DATA_FILE_NAME);
  }

  public Path metaDataPath() {
    return seedDirectory().resolve(META_DATA_FILE_NAME);
  }

  public Path serialLogPath() {
    return workingDir().resolve(SERIAL_LOG_FILE_NAME);
  }

  public List<String> startCommand() {
    var config = architectureConfig();

    var command =
        new ArrayList<>(
            List.of(
                config.qemuBinary(),
                "-machine",
                config.machine(),
                "-cpu",
                config.cpu(),
                "-smp",
                CPU_COUNT,
                "-m",
                MEMORY_MB));

    config.biosPath().ifPresent(biosPath -> command.addAll(List.of("-bios", biosPath)));

    command.addAll(
        List.of(
            "-drive",
            "if=virtio,file=" + diskPath() + ",format=qcow2",
            "-drive",
            "if=virtio,file=" + seedIsoPath() + ",format=raw,readonly=on",
            "-netdev",
            SSH_PORT_FORWARD,
            "-device",
            "virtio-net-pci,netdev=net0",
            "-monitor",
            "none",
            "-serial",
            "file:" + serialLogPath(),
            "-display",
            "none"));

    command.addAll(config.additionalArgs());
    return List.copyOf(command);
  }

  public String imageUrl() {
    return architectureConfig().imageUrl();
  }

  public String qemuBinary() {
    return architectureConfig().qemuBinary();
  }

  public Path logPath() {
    return workingDir().resolve("docktor-qemu.log");
  }

  public Path pidPath() {
    return workingDir().resolve("docktor-qemu.pid");
  }
}
