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
package org.sonar.iac.helm.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.visitors.InputFileContext;

public final class HelmFilesystemUtils {
  private static final Logger LOG = LoggerFactory.getLogger(HelmFilesystemUtils.class);
  private static final Set<String> INCLUDED_EXTENSIONS = Set.of("yaml", "yml", "tpl", "txt", "toml", "properties");

  private HelmFilesystemUtils() {
  }

  // TODO: SONARIAC-1239 Ignore additional file pattern mentioned in .helmignore
  public static Map<String, InputFile> additionalFilesOfHelmProjectDirectory(InputFileContext inputFileContext) {
    Map<String, InputFile> result = new HashMap<>();

    var helmDirectoryPath = retrieveHelmProjectFolder(Path.of(inputFileContext.inputFile.uri()), inputFileContext.sensorContext.fileSystem().baseDir());
    if (helmDirectoryPath == null) {
      LOG.debug("Failed to resolve Helm project directory for {}", inputFileContext.inputFile.uri());
      return result;
    }

    var filePredicate = additionalHelmDependenciesPredicate(inputFileContext, helmDirectoryPath);
    Iterable<InputFile> inputFiles = inputFileContext.sensorContext.fileSystem().inputFiles(filePredicate);

    for (InputFile additionalFile : inputFiles) {
      String fileName = resolveToInputFile(helmDirectoryPath, additionalFile);
      result.put(normalizeToUnixPathSeparator(fileName), additionalFile);
    }
    return result;
  }

  static FilePredicate additionalHelmDependenciesPredicate(InputFileContext inputFileContext, Path helmProjectDirectoryPath) {
    FilePredicates predicates = inputFileContext.sensorContext.fileSystem().predicates();
    // Can be null or throw error?

    String pathPattern = null;

    var basePath = normalizePathForWindows(inputFileContext.sensorContext.fileSystem().baseDir().toPath());
    helmProjectDirectoryPath = normalizePathForWindows(helmProjectDirectoryPath);

    if (basePath != null && helmProjectDirectoryPath != null) {
      var relativizedPath = basePath.relativize(helmProjectDirectoryPath);
      pathPattern = relativizedPath + File.separator + "**";
    }

    if (pathPattern == null) {
      return predicates.none();
    }
    return predicates.and(
      predicates.matchesPathPattern(pathPattern),
      extensionPredicate(predicates),
      predicates.not(predicates.hasURI(inputFileContext.inputFile.uri())));
  }

  private static FilePredicate extensionPredicate(FilePredicates predicates) {
    Set<FilePredicate> extensionPredicates = INCLUDED_EXTENSIONS.stream()
      .map(predicates::hasExtension)
      .collect(Collectors.toSet());

    return predicates.or(extensionPredicates);
  }

  @CheckForNull
  public static Path retrieveHelmProjectFolder(Path inputFilePath, File baseDir) {
    var baseDirPath = normalizePathForWindows(baseDir.toPath());

    if (baseDirPath == null) {
      return null;
    }

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

  public static Path normalizePathForWindows(Path path) {
    return Path.of(normalizeToUnixPathSeparator(path.toString()));
  }
}
