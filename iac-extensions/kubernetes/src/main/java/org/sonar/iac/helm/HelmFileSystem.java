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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.IndexedFile;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.ParseException;

public final class HelmFileSystem {
  private static final Set<String> INCLUDED_EXTENSIONS = Set.of("yaml", "yml", "tpl", "txt", "toml", "properties");
  private final FileSystem fileSystem;

  public HelmFileSystem(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public String getFileRelativePath(IndexedFile indexedInputFile) {
    var filePath = Path.of(indexedInputFile.uri());
    var chartRootDirectory = retrieveHelmProjectFolder(filePath);
    String fileRelativePath;
    if (chartRootDirectory == null) {
      fileRelativePath = indexedInputFile.filename();
    } else {
      fileRelativePath = chartRootDirectory.relativize(filePath).normalize().toString();
      // transform windows to unix path
      fileRelativePath = normalizeToUnixPathSeparator(fileRelativePath);
    }
    return fileRelativePath;
  }

  // Ignore additional file pattern mentioned in .helmignore
  public Map<String, InputFile> getRelatedHelmFiles(InputFile inputFile) {
    var helmDirectoryPath = retrieveHelmProjectFolder(Path.of(inputFile.uri()));
    if (helmDirectoryPath == null) {
      throw new ParseException("Failed to evaluate Helm file " + inputFile + ": Failed to resolve Helm project directory", null, null);
    }

    var additionalHelmFilesPredicate = additionalHelmDependenciesPredicate(inputFile, helmDirectoryPath);
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(additionalHelmFilesPredicate);

    Map<String, InputFile> result = new HashMap<>();
    for (InputFile additionalFile : inputFiles) {
      String fileName = resolveToInputFile(helmDirectoryPath, additionalFile);
      fileName = normalizeToUnixPathSeparator(fileName);
      result.put(fileName, additionalFile);
    }
    return result;
  }

  FilePredicate additionalHelmDependenciesPredicate(InputFile inputFile, Path helmProjectDirectoryPath) {
    FilePredicates predicates = fileSystem.predicates();
    var basePath = fileSystem.baseDir().toPath();
    var relativizedPath = basePath.relativize(helmProjectDirectoryPath);
    String pathPattern = relativizedPath + File.separator + "**";

    return predicates.and(
      predicates.matchesPathPattern(pathPattern),
      extensionPredicate(predicates),
      predicates.not(predicates.hasURI(inputFile.uri())));
  }

  private static FilePredicate extensionPredicate(FilePredicates predicates) {
    Set<FilePredicate> extensionPredicates = INCLUDED_EXTENSIONS.stream()
      .map(predicates::hasExtension)
      .collect(Collectors.toSet());

    return predicates.or(extensionPredicates);
  }

  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem) {
    var baseDirPath = fileSystem.baseDir().toPath();

    var helmProjectDirectoryPath = inputFilePath;

    while (helmProjectDirectoryPath != null && helmProjectDirectoryPath.startsWith(baseDirPath)) {
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

  @CheckForNull
  public Path retrieveHelmProjectFolder(Path inputFilePath) {
    return retrieveHelmProjectFolder(inputFilePath, fileSystem);
  }

  private static String resolveToInputFile(Path helmDirectoryPath, InputFile additionalFile) {
    return helmDirectoryPath.relativize(Path.of(additionalFile.uri())).toString();
  }

  public static String normalizeToUnixPathSeparator(String filename) {
    return filename.replace('\\', '/');
  }
}
