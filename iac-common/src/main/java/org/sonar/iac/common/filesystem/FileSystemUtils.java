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
package org.sonar.iac.common.filesystem;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.SonarRuntimeUtils;

public class FileSystemUtils {
  private FileSystemUtils() {
  }

  /**
   * Reads a file referenced relative to {@code workingDirectory}.
   * Returns empty when the reference is absent, escapes that directory, the directory is unknown, or the file is not part of the analysis-scoped {@code fileSystem} index.
   * Resolution is exact — a namesake elsewhere in the project is never substituted.
   * <p>
   * Skipped entirely in a SonarLint context, since a single-file analysis there does not reliably index the whole project.
   * Revisit once we expose referenced files there ourselves, e.g. via a {@code @SonarLintSide(MODULE)} component backed by SonarLint's {@code ModuleFileSystem}.
   */
  public static Optional<String> readReferencedFile(SensorContext sensorContext, @Nullable Path workingDirectory, String referencedPath) {
    if (workingDirectory == null || SonarRuntimeUtils.isSonarLintContext(sensorContext.runtime()) || !isLocalReference(referencedPath)) {
      return Optional.empty();
    }
    var candidate = workingDirectory.resolve(referencedPath).normalize();
    // Canonical forms on both sides, so that a symlink cannot be used to step out of the working directory
    var canonicalCandidate = canonical(candidate);
    if (!canonicalCandidate.startsWith(canonical(workingDirectory))) {
      return Optional.empty();
    }
    return readIndexedFile(sensorContext.fileSystem(), candidate, canonicalCandidate);
  }

  /**
   * Directory holding {@code file}, null when {@link InputFile#uri()} is not a path on the default file system.
   */
  @Nullable
  public static Path directoryOf(InputFile file) {
    var path = pathOrNull(file.uri());
    return path != null ? path.getParent() : null;
  }

  static boolean isLocalReference(String referencedPath) {
    if (referencedPath.isBlank() || referencedPath.contains("://") || referencedPath.contains("$") || referencedPath.startsWith("~")) {
      return false;
    }
    try {
      return !Path.of(referencedPath).isAbsolute();
    } catch (InvalidPathException e) {
      return false;
    }
  }

  private static Optional<String> readIndexedFile(FileSystem fileSystem, Path candidate, Path canonicalCandidate) {
    var inputFile = indexedFile(fileSystem, candidate);
    if (inputFile == null && !canonicalCandidate.equals(candidate)) {
      inputFile = indexedFile(fileSystem, canonicalCandidate);
    }
    if (inputFile == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(inputFile.contents());
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  @Nullable
  private static InputFile indexedFile(FileSystem fileSystem, Path path) {
    try {
      // hasAbsolutePath() is scanner-only; SonarLint throws UnsupportedOperationException for it, so we use hasURI() instead.
      return fileSystem.inputFile(fileSystem.predicates().hasURI(path.toUri()));
    } catch (UnsupportedOperationException e) {
      return null;
    }
  }

  // InputFile.uri() is not guaranteed to use the file scheme; Path.of would throw for any other scheme.
  @Nullable
  private static Path pathOrNull(URI uri) {
    try {
      return Path.of(uri);
    } catch (IllegalArgumentException | FileSystemNotFoundException e) {
      return null;
    }
  }

  /**
   * Canonicalizes {@code path}, resolving symlinks where the platform allows it — not reliably on Windows, see {@link #retrieveHelmProjectFolder}.
   * The path may not fully exist on disk, so we canonicalize the deepest existing ancestor and re-append the rest literally to keep paths consistent.
   */
  static Path canonical(Path path) {
    var normalized = path.toAbsolutePath().normalize();
    Deque<Path> missingSegments = new ArrayDeque<>();
    var existingAncestor = deepestExistingAncestor(normalized, missingSegments);
    if (existingAncestor == null) {
      return normalized;
    }
    try {
      var resolved = existingAncestor.toFile().getCanonicalFile().toPath();
      for (var segment : missingSegments) {
        resolved = resolved.resolve(segment);
      }
      return resolved;
    } catch (IOException | UnsupportedOperationException e) {
      return normalized;
    }
  }

  @Nullable
  private static Path deepestExistingAncestor(Path path, Deque<Path> missingSegments) {
    var ancestor = path;
    while (ancestor != null && !Files.exists(ancestor)) {
      missingSegments.addFirst(ancestor.getFileName());
      ancestor = ancestor.getParent();
    }
    return ancestor;
  }

  /**
   * Returns a path where Chart.yaml file is located.
   * This is a version for SonarQube and SonarCloud context.
   */
  @Nullable
  public static Path retrieveHelmProjectFolder(Path inputFilePath, FileSystem fileSystem) {
    return retrieveHelmProjectFolder(inputFilePath, fileSystem, Files::exists);
  }

  @Nullable
  public static Path retrieveHelmProjectFolder(@Nullable Path inputFilePath, FileSystem fileSystem, Predicate<Path> chartYamlExist) {
    if (inputFilePath == null) {
      return null;
    }
    var baseDirPath = fileSystem.baseDir().toPath();
    // Resolve the absolute path, in order to not get a short path on Windows system
    try {
      inputFilePath = inputFilePath.toFile().getCanonicalFile().toPath();
      baseDirPath = baseDirPath.toFile().getCanonicalFile().toPath();
    } catch (IOException | UnsupportedOperationException e) {
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
