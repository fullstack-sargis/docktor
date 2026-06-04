package dev.docktor.qemu;

import static org.assertj.core.api.Assertions.assertThat;

import dev.docktor.core.ArchitectureDetector;
import org.junit.jupiter.api.Test;

class QemuConfigGeneratorTest {
  @Test
  void generatesArm64QemuStartCommand() {
    var generator = new QemuConfigGenerator(new FixedArchitecture("arm64"));

    assertThat(generator.startCommand())
        .contains(
            "qemu-system-aarch64",
            "/opt/homebrew/share/qemu/edk2-aarch64-code.fd",
            "-machine",
            "virt,accel=hvf",
            "-smp",
            "4",
            "-m",
            "4096",
            "-netdev",
            "user,id=net0,hostfwd=tcp::2222-:22",
            "file:" + generator.workingDir().resolve("serial.log"),
            "-display",
            "none");
  }

  @Test
  void selectsArm64ConfigForArm64Aliases() {
    assertThat(
            new QemuConfigGenerator(new FixedArchitecture("arm64"))
                .architectureConfig()
                .architecture())
        .isEqualTo("arm64");
    assertThat(
            new QemuConfigGenerator(new FixedArchitecture("aarch64"))
                .architectureConfig()
                .architecture())
        .isEqualTo("arm64");
  }

  @Test
  void selectsX8664ConfigForX8664Aliases() {
    assertThat(
            new QemuConfigGenerator(new FixedArchitecture("x86_64"))
                .architectureConfig()
                .architecture())
        .isEqualTo("x86_64");
    assertThat(
            new QemuConfigGenerator(new FixedArchitecture("amd64"))
                .architectureConfig()
                .architecture())
        .isEqualTo("x86_64");
  }

  @Test
  void imagePathsAndUrlsAreArchitectureSpecific() {
    var arm64 = new QemuConfigGenerator(new FixedArchitecture("arm64"));
    var x8664 = new QemuConfigGenerator(new FixedArchitecture("x86_64"));

    assertThat(arm64.imagePath().toString()).endsWith("ubuntu-24.04-server-cloudimg-arm64.img");
    assertThat(arm64.imageUrl()).endsWith("ubuntu-24.04-server-cloudimg-arm64.img");
    assertThat(x8664.imagePath().toString()).endsWith("ubuntu-24.04-server-cloudimg-amd64.img");
    assertThat(x8664.imageUrl()).endsWith("ubuntu-24.04-server-cloudimg-amd64.img");
  }

  @Test
  void cloudInitFilesAreWrittenInsideSeedDirectory() {
    var generator = new QemuConfigGenerator();

    assertThat(generator.seedDirectory().toString()).endsWith(".docktor/qemu/seed");
    assertThat(generator.userDataPath()).isEqualTo(generator.seedDirectory().resolve("user-data"));
    assertThat(generator.metaDataPath()).isEqualTo(generator.seedDirectory().resolve("meta-data"));
    assertThat(generator.seedCdrPath().toString()).endsWith(".docktor/qemu/seed.iso.cdr");
  }

  private static class FixedArchitecture extends ArchitectureDetector {
    private final String architecture;

    private FixedArchitecture(String architecture) {
      this.architecture = architecture;
    }

    @Override
    public String currentArchitecture() {
      return architecture;
    }
  }
}
