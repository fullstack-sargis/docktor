package dev.docktor.qemu.cloudinit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CloudInitConfigTest {
  @TempDir Path tempDir;

  @Test
  void userDataInstallsDockerAndAddsSshPublicKey() throws Exception {
    var publicKeyPath = tempDir.resolve("id_rsa.pub");
    Files.writeString(publicKeyPath, "ssh-rsa test-key docktor@example.local\n");

    var userData = new CloudInitConfig(publicKeyPath).userData();

    assertThat(userData).contains("#cloud-config");
    assertThat(userData).contains("name: docktor");
    assertThat(userData).contains("sudo: ALL=(ALL) NOPASSWD:ALL");
    assertThat(userData).contains("lock_passwd: true");
    assertThat(userData).contains("ssh_authorized_keys:");
    assertThat(userData).contains("ssh-rsa test-key docktor@example.local");
    assertThat(userData).contains("ssh_pwauth: false");
    assertThat(userData).contains("docker.io");
    assertThat(userData).contains("usermod -aG docker docktor");
    assertThat(userData).contains("systemctl enable docker");
    assertThat(userData).contains("systemctl start docker");
  }

  @Test
  void metaDataIdentifiesDocktorVm() {
    var metaData = new CloudInitConfig(tempDir.resolve("unused.pub")).metaData();

    assertThat(metaData).contains("instance-id: docktor-qemu");
    assertThat(metaData).contains("local-hostname: docktor-qemu");
  }

  @Test
  void userDataFailsWhenSshPublicKeyIsMissing() {
    var missingKey = tempDir.resolve("missing.pub");

    assertThatThrownBy(() -> new CloudInitConfig(missingKey).userData())
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SSH public key not found: " + missingKey);
  }
}
