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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

public final class HelmFileSystem {
  private static final Logger LOG = LoggerFactory.getLogger(HelmFileSystem.class);
  private static final Set<String> INCLUDED_EXTENSIONS = Set.of("yaml", "yml", "tpl", "txt", "toml", "properties");
  private final FileSystem fileSystem;

  public HelmFileSystem(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  // Ignore additional file pattern mentioned in .helmignore
  public Map<String, InputFile> getRelatedHelmFiles(InputFile inputFile) {
    var helmDirectoryPath = retrieveHelmProjectFolder(Path.of(inputFile.uri()), fileSystem.baseDir());
    if (helmDirectoryPath == null) {
      LOG.debug("Failed to resolve Helm project directory for {}", inputFile.uri());
      return Collections.emptyMap();
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

  @CheckForNull
  public static Path retrieveHelmProjectFolder(Path inputFilePath, File baseDir) {
    var baseDirPath = baseDir.toPath();

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

  private static String resolveToInputFile(Path helmDirectoryPath, InputFile additionalFile) {
    return helmDirectoryPath.relativize(Path.of(additionalFile.uri())).toString();
  }

  public static String normalizeToUnixPathSeparator(String filename) {
    return filename.replace('\\', '/');
  }
}
