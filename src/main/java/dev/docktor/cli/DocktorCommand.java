package dev.docktor.cli;

import picocli.CommandLine.Command;

@Command(
    name = "docktor",
    mixinStandardHelpOptions = true,
    version = "docktor 0.1.0",
    description = "Diagnose and manage a local Docker VM environment on macOS.")
public class DocktorCommand {}
