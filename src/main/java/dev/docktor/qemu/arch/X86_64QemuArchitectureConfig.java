package dev.docktor.qemu.arch;

import java.util.Optional;

public class X86_64QemuArchitectureConfig implements QemuArchitectureConfig {
  private static final String ARCHITECTURE = "x86_64";
  private static final String IMAGE_FILE_NAME = "ubuntu-24.04-server-cloudimg-amd64.img";
  private static final String IMAGE_URL =
      "https://cloud-images.ubuntu.com/releases/24.04/release/ubuntu-24.04-server-cloudimg-amd64.img";
  private static final String QEMU_BINARY = "qemu-system-x86_64";
  private static final String MACHINE = "q35,accel=hvf";
  private static final String CPU = "host";

  @Override
  public String architecture() {
    return ARCHITECTURE;
  }

  @Override
  public String imageFileName() {
    return IMAGE_FILE_NAME;
  }

  @Override
  public String imageUrl() {
    return IMAGE_URL;
  }

  @Override
  public String qemuBinary() {
    return QEMU_BINARY;
  }

  @Override
  public String machine() {
    return MACHINE;
  }

  @Override
  public String cpu() {
    return CPU;
  }

  @Override
  public Optional<String> biosPath() {
    return Optional.empty();
  }
}
