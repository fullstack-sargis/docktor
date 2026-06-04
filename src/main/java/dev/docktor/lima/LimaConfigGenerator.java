package dev.docktor.lima;

public class LimaConfigGenerator {
  public String generateDefaultConfig(String architecture) {
    return """
                # TODO: Docktor Lima VM configuration
                # Architecture: %s
                images: []
                mounts: []
                provision: []
                """
        .formatted(architecture);
  }
}
