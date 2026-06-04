package dev.docktor.qemu.cloudinit;

import dev.docktor.core.CommandRunner;
import dev.docktor.qemu.QemuCommandFailure;
import dev.docktor.qemu.QemuConfigGenerator;
import java.io.IOException;
import java.nio.file.Files;

public class CloudInitManager {
  private static final String HDIUTIL = "hdiutil";
  private static final String SEED_VOLUME_NAME = "cidata";

  private final CommandRunner commandRunner;
  private final QemuConfigGenerator configGenerator;
  private final CloudInitConfig cloudInitConfig;

  public CloudInitManager(CommandRunner commandRunner, QemuConfigGenerator configGenerator) {
    this(commandRunner, configGenerator, new CloudInitConfig());
  }

  CloudInitManager(
      CommandRunner commandRunner,
      QemuConfigGenerator configGenerator,
      CloudInitConfig cloudInitConfig) {
    this.commandRunner = commandRunner;
    this.configGenerator = configGenerator;
    this.cloudInitConfig = cloudInitConfig;
  }

  public void prepare() throws IOException, InterruptedException {
    writeCloudInitFiles();
    createSeedIso();
  }

  private void writeCloudInitFiles() throws IOException {
    Files.createDirectories(configGenerator.seedDirectory());
    Files.writeString(configGenerator.userDataPath(), cloudInitConfig.userData());
    Files.writeString(configGenerator.metaDataPath(), cloudInitConfig.metaData());
  }

  private void createSeedIso() throws IOException, InterruptedException {
    Files.deleteIfExists(configGenerator.seedIsoPath());
    Files.deleteIfExists(configGenerator.seedCdrPath());

    var result =
        commandRunner.run(
            HDIUTIL,
            "makehybrid",
            "-o",
            configGenerator.seedIsoPath().toString(),
            configGenerator.seedDirectory().toString(),
            "-iso",
            "-joliet",
            "-default-volume-name",
            SEED_VOLUME_NAME);

    if (!result.successful()) {
      throw new IOException(
          "Failed to create NoCloud seed ISO: " + QemuCommandFailure.reason(result));
    }

    if (!Files.exists(configGenerator.seedIsoPath())
        && Files.exists(configGenerator.seedCdrPath())) {
      Files.move(configGenerator.seedCdrPath(), configGenerator.seedIsoPath());
    }
  }
}
