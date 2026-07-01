/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.predicates;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Cache of the {@link FileType} computed for each file by {@link YamlFileTypeResolver}, shared by all YAML based sensors
 * of a single analysis so that a file's type is computed only once even if several sensors are interested in it.
 * <p>
 * Besides the {@code URI -> FileType} lookup, the cache also maintains the reverse index {@code FileType -> InputFiles},
 * so a sensor can ask for all the files resolved to a given {@link FileType} (through {@link #getFiles}) instead of
 * re-applying a predicate over the whole file system.
 * <p>
 * It is scoped to a single analysis ({@link SonarLintSide.Lifespan#SINGLE_ANALYSIS}), the same lifespan as
 * {@link YamlFileTypeResolver}: a fresh cache is built for every analysis, so it can safely hold that analysis'
 * {@link InputFile} instances and never serves a stale type - a new analysis simply starts from an empty cache.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeCache {

  private final Map<URI, FileType> fileTypeCache = new ConcurrentHashMap<>();
  private final Map<FileType, Set<InputFile>> filesByType = new ConcurrentHashMap<>();

  public YamlFileTypeCache() {
    // Public explicit constructor for injection
  }

  @CheckForNull
  public FileType get(URI fileUri) {
    return fileTypeCache.get(fileUri);
  }

  public void put(InputFile inputFile, FileType fileType) {
    var previous = fileTypeCache.put(inputFile.uri(), fileType);
    if (previous != null && previous != fileType) {
      removeFromReverseIndex(previous, inputFile);
    }
    addToReverseIndex(fileType, inputFile);
  }

  /**
   * Returns the input files resolved to any of the given {@link FileType}s. The returned set is a snapshot: it is
   * detached from the cache, so iterating it is safe even if the cache is concurrently mutated.
   */
  public Set<InputFile> getFiles(FileType... fileTypes) {
    var result = new HashSet<InputFile>();
    for (var fileType : fileTypes) {
      var files = filesByType.get(fileType);
      if (files != null) {
        result.addAll(files);
      }
    }
    return result;
  }

  private void addToReverseIndex(FileType fileType, InputFile inputFile) {
    if (fileType != FileType.UNDETERMINED) {
      filesByType.computeIfAbsent(fileType, type -> ConcurrentHashMap.newKeySet()).add(inputFile);
    }
  }

  private void removeFromReverseIndex(FileType fileType, InputFile inputFile) {
    var files = filesByType.get(fileType);
    if (files != null) {
      files.remove(inputFile);
    }
  }
}
