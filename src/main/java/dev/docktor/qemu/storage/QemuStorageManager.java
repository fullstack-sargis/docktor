package dev.docktor.qemu.storage;

import dev.docktor.core.CommandRunner;
import dev.docktor.qemu.QemuCommandFailure;
import dev.docktor.qemu.QemuConfigGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class QemuStorageManager {
  private static final String CURL = "curl";
  private static final String QEMU_IMG = "qemu-img";
  private static final String DISK_SIZE = "20G";
  private static final String KNOWN_HOST = "[127.0.0.1]:2222";

  private final CommandRunner commandRunner;
  private final QemuConfigGenerator configGenerator;

  public QemuStorageManager(CommandRunner commandRunner, QemuConfigGenerator configGenerator) {
    this.commandRunner = commandRunner;
    this.configGenerator = configGenerator;
  }

  public void prepareStorage() throws IOException, InterruptedException {
    Files.createDirectories(configGenerator.workingDir());
    downloadImageIfMissing();
    createDiskIfMissing();
  }

  public void resetStorage() throws IOException {
    Files.deleteIfExists(configGenerator.diskPath());
    Files.deleteIfExists(configGenerator.seedIsoPath());
    Files.deleteIfExists(configGenerator.seedCdrPath());
    Files.deleteIfExists(configGenerator.pidPath());
    Files.deleteIfExists(configGenerator.logPath());
    Files.deleteIfExists(configGenerator.serialLogPath());
    removeKnownHost();
  }

  private void downloadImageIfMissing() throws IOException, InterruptedException {
    var imagePath = configGenerator.imagePath();
    if (Files.exists(imagePath)) {
      return;
    }

    var result =
        commandRunner.runInteractive(
            List.of(CURL, "-L", "-o", imagePath.toString(), configGenerator.imageUrl()));

    if (!result.successful()) {
      throw new IOException(
          "Failed to download Ubuntu cloud image: " + QemuCommandFailure.reason(result));
    }
  }

  private void createDiskIfMissing() throws IOException, InterruptedException {
    if (Files.exists(configGenerator.diskPath())) {
      return;
    }

    var result =
        commandRunner.run(
            List.of(
                QEMU_IMG,
                "create",
                "-f",
                "qcow2",
                "-F",
                "qcow2",
                "-b",
                configGenerator.imagePath().toString(),
                configGenerator.diskPath().toString(),
                DISK_SIZE));

    if (!result.successful()) {
      throw new IOException("Failed to create QEMU disk: " + QemuCommandFailure.reason(result));
    }
  }

  private void removeKnownHost() {
    try {
      commandRunner.run("ssh-keygen", "-R", KNOWN_HOST);
    } catch (IOException ignored) {
      // Reset should not fail because known_hosts cleanup is best-effort.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
