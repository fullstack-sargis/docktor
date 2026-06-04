package dev.docktor.qemu.arch;

import java.util.List;
import java.util.Optional;

public interface QemuArchitectureConfig {
  String architecture();

  String imageFileName();

  String imageUrl();

  String qemuBinary();

  String machine();

  String cpu();

  Optional<String> biosPath();

  default List<String> additionalArgs() {
    return List.of();
  }
}
