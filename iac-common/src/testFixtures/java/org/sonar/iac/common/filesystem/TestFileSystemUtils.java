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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public final class TestFileSystemUtils {

  private TestFileSystemUtils() {
  }

  /**
   * Resolves relative to the analyzed file's own directory, for test harnesses with a real path and no {@code SensorContext}.
   * Production code should use {@link FileSystemUtils#readReferencedFile}.
   */
  public static Optional<String> readReferencedFileRelativeTo(@Nullable Path analyzedFile, String referencedPath) {
    if (analyzedFile == null || !FileSystemUtils.isLocalReference(referencedPath)) {
      return Optional.empty();
    }
    var analyzedDir = analyzedFile.getParent();
    if (analyzedDir == null) {
      return Optional.empty();
    }
    var normalized = FileSystemUtils.canonical(analyzedDir.resolve(referencedPath).normalize());
    var bound = FileSystemUtils.canonical(analyzedDir);
    if (!normalized.startsWith(bound) || !Files.isRegularFile(normalized)) {
      return Optional.empty();
    }
    try {
      return Optional.of(Files.readString(normalized));
    } catch (IOException e) {
      return Optional.empty();
    }
  }
}
