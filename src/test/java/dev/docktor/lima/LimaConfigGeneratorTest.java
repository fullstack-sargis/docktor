package dev.docktor.lima;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LimaConfigGeneratorTest {
  @Test
  void generatesArchitectureAwareConfigPlaceholder() {
    var config = new LimaConfigGenerator().generateDefaultConfig("arm64");

    assertThat(config).contains("Architecture: arm64");
    assertThat(config).contains("provision: []");
  }
}
