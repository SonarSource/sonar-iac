/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.common.filesystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;
import org.sonar.api.batch.fs.FileSystem;

public class FileSystemUtils {
  private FileSystemUtils() {
  }

  /**
   * Returns a path where Chart.yaml file is located.
   * This is a version for SonarQube and SonarCloud context.
   */
  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem) {
    return retrieveHelmProjectFolder(inputFilePath, fileSystem, Files::exists);
  }

  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem, Predicate<Path> chartYamlExist) {
    var baseDirPath = fileSystem.baseDir().toPath();
    // Resolve the absolute path, in order to not get a short path on Windows system
    try {
      baseDirPath = baseDirPath.toFile().getCanonicalFile().toPath();
    } catch (IOException e) {
      // In case of error, we keep the original baseDirPath
    }

    var helmProjectDirectoryPath = inputFilePath;

    while (helmProjectDirectoryPath != null) {
      if (chartYamlExist.test(helmProjectDirectoryPath.resolve("Chart.yaml"))) {
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
