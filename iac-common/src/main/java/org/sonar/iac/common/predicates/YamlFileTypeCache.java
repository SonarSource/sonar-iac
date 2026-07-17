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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.CheckForNull;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.scanner.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Cache shared by all YAML based sensors of a single analysis, so a file's {@link FileType} is computed only once.
 * <p>
 * It holds the {@code URI -> FileType} lookup and, per {@link FileSystem}, the ordered candidate files of the
 * classification scan, so a sensor can get its files without re-scanning. It is keyed by {@link FileSystem} because a
 * multi-module analysis ({@code sonar.modules}) builds one file system per module while sharing this analysis-scoped
 * cache, so each module's sensors must get back only their own module's files.
 * <p>
 * Scoped to a single analysis ({@link SonarLintSide.Lifespan#SINGLE_ANALYSIS}): a fresh cache per analysis, so it can
 * safely hold that analysis' {@link InputFile} instances and never serves a stale type.
 */
@ScannerSide
@SonarLintSide(lifespan = SonarLintSide.SINGLE_ANALYSIS)
public class YamlFileTypeCache {

  private final Map<URI, FileType> fileTypeCache = new ConcurrentHashMap<>();
  private final Map<FileSystem, List<InputFile>> classifiedCandidatesByFileSystem = new ConcurrentHashMap<>();

  public YamlFileTypeCache() {
    // Public explicit constructor for injection
  }

  @CheckForNull
  public FileType get(URI fileUri) {
    return fileTypeCache.get(fileUri);
  }

  public void put(InputFile inputFile, FileType fileType) {
    fileTypeCache.put(inputFile.uri(), fileType);
  }

  /**
   * Returns the ordered candidate files already classified for the given {@link FileSystem}, or {@code null} if it has
   * not been classified yet. An empty (non-null) list means the scan ran and found no candidate file: a hit, not a miss.
   */
  @CheckForNull
  public List<InputFile> getClassifiedCandidates(FileSystem fileSystem) {
    return classifiedCandidatesByFileSystem.get(fileSystem);
  }

  /**
   * Memoizes the candidate files - in file-system iteration order - classified for the given {@link FileSystem}, so the
   * classification scan runs only once per file system no matter how many sensors need those files.
   */
  public void putClassifiedCandidates(FileSystem fileSystem, List<InputFile> orderedCandidateFiles) {
    classifiedCandidatesByFileSystem.put(fileSystem, orderedCandidateFiles);
  }
}
