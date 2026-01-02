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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.kubernetes.visitors.HelmInputFileContext;

public class DefaultFileSystemProvider implements FileSystemProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultFileSystemProvider.class);

  private static final Set<String> INCLUDED_EXTENSIONS = Set.of("yaml", "yml", "tpl", "txt", "toml", "properties");

  private final FileSystem fileSystem;

  public DefaultFileSystemProvider(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  @Override
  public Map<String, String> inputFilesForHelm(HelmInputFileContext inputFileContext) {
    var files = getFiles(inputFileContext);
    return validateAndReadFiles(files, inputFileContext);
  }

  private Map<String, InputFile> getFiles(HelmInputFileContext inputFileContext) {
    var helmDirectoryPath = inputFileContext.getHelmProjectDirectory();
    if (helmDirectoryPath == null) {
      return Map.of();
    }

    var additionalHelmFilesPredicate = additionalHelmDependenciesPredicate(inputFileContext.inputFile, helmDirectoryPath);
    Iterable<InputFile> inputFiles = fileSystem.inputFiles(additionalHelmFilesPredicate);

    Map<String, InputFile> result = new HashMap<>();
    for (InputFile additionalFile : inputFiles) {
      String fileName = resolveToInputFile(helmDirectoryPath, additionalFile);
      fileName = FileSystemProvider.normalizeToUnixPathSeparator(fileName);
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

  private static String resolveToInputFile(Path helmDirectoryPath, InputFile additionalFile) {
    return helmDirectoryPath.relativize(Path.of(additionalFile.uri())).toString();
  }

  static Map<String, String> validateAndReadFiles(Map<String, InputFile> filesMap, HelmInputFileContext inputFileContext) {
    var filesMapFiltered = filesMap;
    if (filesMap.keySet().stream().anyMatch(FileSystemProvider::containsLineBreak)) {
      filesMapFiltered = filesMap.entrySet().stream()
        .filter(entry -> !FileSystemProvider.containsLineBreak(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      LOG.debug("Some additional files have names containing line breaks, skipping them");
    }

    Map<String, String> fileContents = new HashMap<>(filesMapFiltered.size());

    for (Map.Entry<String, InputFile> filenameToInputFile : filesMapFiltered.entrySet()) {
      var additionalInputFile = filenameToInputFile.getValue();
      String fileContent;
      try {
        fileContent = additionalInputFile.contents();
      } catch (IOException e) {
        throw parseExceptionFor(inputFileContext.inputFile, "Failed to read file at " + additionalInputFile, e.getMessage());
      }

      fileContents.put(filenameToInputFile.getKey(), fileContent);
    }
    return fileContents;
  }

  private static ParseException parseExceptionFor(InputFile inputFile, String cause, @Nullable String details) {
    return new ParseException("Failed to evaluate Helm file " + inputFile + ": " + cause, null, details);
  }
}
