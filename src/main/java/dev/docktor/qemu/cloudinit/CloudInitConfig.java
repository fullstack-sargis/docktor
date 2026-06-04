package dev.docktor.qemu.cloudinit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class CloudInitConfig {
  private static final String VM_USER = "docktor";
  private static final String INSTANCE_ID = "docktor-qemu";
  private static final String HOSTNAME = "docktor-qemu";

  private final Path publicKeyPath;

  CloudInitConfig() {
    this(Path.of(System.getProperty("user.home"), ".ssh", "id_rsa.pub"));
  }

  CloudInitConfig(Path publicKeyPath) {
    this.publicKeyPath = publicKeyPath;
  }

  String userData() {
    return """
                #cloud-config
                users:
                  - name: %s
                    groups: [sudo, docker]
                    shell: /bin/bash
                    sudo: ALL=(ALL) NOPASSWD:ALL
                    lock_passwd: true
                    ssh_authorized_keys:
                      - %s
                ssh_pwauth: false
                package_update: true
                packages:
                  - docker.io
                runcmd:
                  - usermod -aG docker %s
                  - systemctl enable docker
                  - systemctl start docker
                """
        .formatted(VM_USER, readPublicKey(), VM_USER);
  }

  String metaData() {
    return """
                instance-id: %s
                local-hostname: %s
                """
        .formatted(INSTANCE_ID, HOSTNAME);
  }

  private String readPublicKey() {
    try {
      return Files.readString(publicKeyPath).trim();
    } catch (IOException e) {
      throw new IllegalStateException("SSH public key not found: " + publicKeyPath, e);
    }
  }
}
