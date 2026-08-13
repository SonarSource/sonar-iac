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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Cache shared by all YAML based sensors of a single module, so a file's {@link FileType} is computed only once.
 * Holds the classified files of one {@link FileSystem} at a time; a multi-module analysis ({@code sonar.modules})
 * builds one file system per module, so {@link #clearAndStartClassifyingFor} switches (and clears) the cache when a
 * different one arrives - callers must therefore be module-scoped.
 * <p>
 * Written once per module by the PRE-phase {@link YamlFileTypeClassificationSensor}, then read-only; not thread-safe,
 * as no concurrent writer is expected. Scoped to a single analysis ({@link SonarLintSide.Lifespan#SINGLE_ANALYSIS}): a
 * fresh cache per analysis, so it can safely hold that analysis' {@link InputFile} instances and never serves a stale
 * type.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeCache {

  private static final Logger LOG = LoggerFactory.getLogger(YamlFileTypeCache.class);

  @Nullable
  private FileSystem currentFileSystem;
  private final Set<URI> cachedInputFileURIs = new HashSet<>();
  private final Map<FileType, List<InputFile>> filesPerType = new EnumMap<>(FileType.class);

  public YamlFileTypeCache() {
    // public explicit constructor for injection
  }

  public void clearAndStartClassifyingFor(FileSystem newFileSystem) {
    if (!hasCacheDataFor(newFileSystem)) {
      clearCache();
    }
    this.currentFileSystem = newFileSystem;
  }

  public boolean hasCacheDataFor(FileSystem fileSystem) {
    return fileSystem == currentFileSystem;
  }

  private void clearCache() {
    currentFileSystem = null;
    filesPerType.clear();
    cachedInputFileURIs.clear();
  }

  public List<InputFile> getInputFiles(Set<FileType> fileTypes) {
    var inputFiles = new ArrayList<InputFile>();
    filesPerType.forEach((fileType, files) -> {
      if (fileTypes.contains(fileType)) {
        inputFiles.addAll(files);
      }
    });
    return inputFiles;
  }

  public boolean hasKnownType(InputFile inputFile) {
    return cachedInputFileURIs.contains(inputFile.uri());
  }

  /**
   * Records {@code inputFile}'s {@link FileType}, or throws if it was already recorded with a different type - should
   * be structurally impossible, since {@code YamlFileTypeResolver#classify} assigns each candidate at most once.
   */
  public void putIfUncached(InputFile inputFile, FileType fileType) {
    if (cachedInputFileURIs.add(inputFile.uri())) {
      filesPerType.computeIfAbsent(fileType, key -> new ArrayList<>()).add(inputFile);
    } else {
      var alreadyAssignedFileType = getAssignedFileType(inputFile);
      var message = String.format("Input file '%s' was already classified as '%s' file and can't be reclassified as '%s'", inputFile.uri(), alreadyAssignedFileType, fileType);
      throw new IllegalStateException(message);
    }
  }

  @Nullable
  private FileType getAssignedFileType(InputFile inputFile) {
    for (Map.Entry<FileType, List<InputFile>> fileTypeListEntry : filesPerType.entrySet()) {
      if (fileTypeListEntry.getValue().contains(inputFile)) {
        return fileTypeListEntry.getKey();
      }
    }
    return null;
  }

  public void logClassifiedCount() {
    if (LOG.isDebugEnabled()) {
      String formattedEntries = filesPerType.entrySet().stream()
        .map(entry -> {
          int count = entry.getValue().size();
          String suffix = (count == 1) ? "file" : "files";
          return entry.getKey() + ": " + count + " " + suffix;
        })
        .collect(Collectors.joining(", "));

      LOG.debug("Classified input files for: {}", formattedEntries);
    }
  }
}
