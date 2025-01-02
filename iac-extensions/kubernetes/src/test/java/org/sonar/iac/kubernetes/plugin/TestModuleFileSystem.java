/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
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
