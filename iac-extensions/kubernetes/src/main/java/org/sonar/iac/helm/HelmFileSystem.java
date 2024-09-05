/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.helm;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
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

  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem) {
    var baseDirPath = fileSystem.baseDir().toPath();

    var helmProjectDirectoryPath = inputFilePath;

    while (helmProjectDirectoryPath != null) {
      if (Files.exists(helmProjectDirectoryPath.resolve("Chart.yaml"))) {
        break;
      }
      helmProjectDirectoryPath = helmProjectDirectoryPath.getParent();
    }
    if (helmProjectDirectoryPath != null && !helmProjectDirectoryPath.startsWith(baseDirPath)) {
      return null;
    }
    return helmProjectDirectoryPath;
  }

  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem, SonarLintFileListener sonarLintFileListener) {
    var baseDirPath = fileSystem.baseDir().toPath();

    var helmProjectDirectoryPath = inputFilePath;

    while (helmProjectDirectoryPath != null) {
      if (sonarLintFileListener.inputFilesContents().containsKey(helmProjectDirectoryPath.resolve("Chart.yaml").toUri().toString())) {
        break;
      }
      helmProjectDirectoryPath = helmProjectDirectoryPath.getParent();
    }
    if (helmProjectDirectoryPath != null && !helmProjectDirectoryPath.startsWith(baseDirPath)) {
      return null;
    }
    return helmProjectDirectoryPath;
  }
}
