/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

  public static Map<String, InputFile> retrieveFilesInHelmProject(InputFileContext inputFileContext) {
    Map<String, InputFile> result = new HashMap<>();

    var helmDirectoryPath = retrieveHelmProjectFolder(Path.of(inputFileContext.inputFile.uri()));
    if (helmDirectoryPath == null) {
      LOG.debug("Failed to resolve Helm project directory for {}", inputFileContext.inputFile.uri());
      return result;
    }

    var filePredicate = helmProjectPredicate(inputFileContext, helmDirectoryPath);
    Iterable<InputFile> inputFiles = inputFileContext.sensorContext.fileSystem().inputFiles(filePredicate);

    for (InputFile additionalFile : inputFiles) {
      result.put(resolveToInputFile(helmDirectoryPath, additionalFile), additionalFile);
    }
    return result;
  }

  static FilePredicate helmProjectPredicate(InputFileContext inputFileContext, Path helmProjectDirectoryPath) {
    FilePredicates predicates = inputFileContext.sensorContext.fileSystem().predicates();
    // Can be null or throw error?

    String pathPattern = null;

    try {
      var basePath = inputFileContext.sensorContext.fileSystem().baseDir().toPath().toRealPath();
      var relativizedPath = basePath.relativize(helmProjectDirectoryPath.toRealPath());
      pathPattern = relativizedPath + File.separator + "**";
    } catch (IOException e) {
      LOG.debug("Failed to resolve Helm project file predicate for {}", inputFileContext.inputFile.uri());
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

  static Path retrieveHelmProjectFolder(Path inputFilePath) {
    var templateDirectoryPath = inputFilePath.getParent();

    if (templateDirectoryPath != null) {
      templateDirectoryPath = templateDirectoryPath.getParent();
    }
    return templateDirectoryPath;
  }

  private static String resolveToInputFile(Path helmDirectoryPath, InputFile additionalFile) {
    return helmDirectoryPath.relativize(Path.of(additionalFile.uri())).toString();
  }

}
