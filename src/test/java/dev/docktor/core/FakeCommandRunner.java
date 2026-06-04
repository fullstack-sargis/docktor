package dev.docktor.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeCommandRunner implements CommandRunner {
  private final Map<List<String>, ProcessResult> results = new HashMap<>();
  private final List<List<String>> commands = new ArrayList<>();

  public void returns(List<String> command, ProcessResult result) {
    results.put(command, result);
  }

  public List<List<String>> commands() {
    return commands;
  }

  @Override
  public ProcessResult run(List<String> command) throws IOException {
    commands.add(command);
    return results.getOrDefault(command, new ProcessResult(1, "", "not found"));
  }
}
