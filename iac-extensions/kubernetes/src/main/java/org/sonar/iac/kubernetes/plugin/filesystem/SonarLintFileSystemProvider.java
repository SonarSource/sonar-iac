/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin.filesystem;

import java.util.Map;
import org.sonar.iac.kubernetes.plugin.SonarLintFileListener;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

import static java.util.stream.Collectors.toMap;

public class SonarLintFileSystemProvider implements FileSystemProvider {

  private final SonarLintFileListener sonarLintFileListener;

  public SonarLintFileSystemProvider(SonarLintFileListener sonarLintFileListener) {
    this.sonarLintFileListener = sonarLintFileListener;
  }

  @Override
  public Map<String, String> inputFilesForHelm(HelmInputFileContext inputFileContext) {
    var helmProjectDirectory = inputFileContext.getHelmProjectDirectory();
    if (helmProjectDirectory != null) {
      var helmProjectDirectoryAsText = helmProjectDirectory.toUri().toString();
      return sonarLintFileListener.inputFilesContents().entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(helmProjectDirectoryAsText))
        .filter(entry -> !entry.getKey().equals(inputFileContext.inputFile.uri().toString()))
        .collect(toMap(entry -> entry.getKey().substring(helmProjectDirectoryAsText.length()), Map.Entry::getValue));
    }
    return Map.of();
  }
}
