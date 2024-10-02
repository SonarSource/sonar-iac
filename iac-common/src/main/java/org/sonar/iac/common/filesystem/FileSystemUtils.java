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
package org.sonar.iac.common.filesystem;

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
