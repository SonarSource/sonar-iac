package org.sonar.iac.kubernetes.plugin;

import java.util.List;
import java.util.stream.Stream;
import org.sonar.api.batch.fs.InputFile;
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileSystem;

public class TestModuleFileSystem implements ModuleFileSystem {
  private final List<InputFile> inputFiles;

  public TestModuleFileSystem(List<InputFile> inputFiles) {
    this.inputFiles = inputFiles;
  }

  @Override
  public Stream<InputFile> files(String s, InputFile.Type type) {
    return inputFiles.stream();
  }

  @Override
  public Stream<InputFile> files() {
    return inputFiles.stream();
  }
}
