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
package org.sonar.iac.helm;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.filesystem.FileSystemUtils;
import org.sonar.iac.kubernetes.plugin.SonarLintFileListener;
import org.sonar.iac.kubernetes.plugin.filesystem.FileSystemProvider;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

public final class HelmFileSystem {
  public static final Set<String> INCLUDED_EXTENSIONS = Set.of("yaml", "yml", "tpl", "txt", "toml", "properties");
  private final FileSystemProvider fileSystemProvider;

  public HelmFileSystem(FileSystemProvider fileSystemProvider) {
    this.fileSystemProvider = fileSystemProvider;
  }

  public static String getFileRelativePath(HelmInputFileContext inputFileContext) {
    var inputFile = inputFileContext.inputFile;
    var chartRootDirectory = inputFileContext.getHelmProjectDirectory();
    return getFileRelativePath(chartRootDirectory, inputFile);
  }

  public static String getFileRelativePath(@Nullable Path chartRootDirectory, InputFile inputFile) {
    var filePath = Path.of(inputFile.uri());
    String fileRelativePath;
    if (chartRootDirectory == null) {
      fileRelativePath = inputFile.filename();
    } else {
      fileRelativePath = chartRootDirectory.relativize(filePath).normalize().toString();
      // transform windows to unix path
      fileRelativePath = FileSystemProvider.normalizeToUnixPathSeparator(fileRelativePath);
    }
    return fileRelativePath;
  }

  public Map<String, String> getRelatedHelmFiles(HelmInputFileContext inputFileContext) {
    return fileSystemProvider.inputFilesForHelm(inputFileContext);
  }

  /**
   * Returns a path where Chart.yaml file is located.
   * This is a version for SonarLint context.
   */
  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem, SonarLintFileListener sonarLintFileListener) {
    return FileSystemUtils.retrieveHelmProjectFolder(inputFilePath, fileSystem, path -> fileExistInSonarLint(sonarLintFileListener, path));
  }

  private static boolean fileExistInSonarLint(SonarLintFileListener sonarLintFileListener, Path helmProjectDirectoryPath) {
    return sonarLintFileListener.inputFilesContents().containsKey(helmProjectDirectoryPath.toUri().toString());
  }
}
