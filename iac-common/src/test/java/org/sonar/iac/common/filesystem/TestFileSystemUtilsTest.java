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
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.abort;

class TestFileSystemUtilsTest {

  @TempDir
  private Path tmpDir;

  private Path appDir;
  private Path analyzedFile;
  private final TestFileSystemUtils.ReferencedFileTestCache cache = new TestFileSystemUtils.ReferencedFileTestCache();
  private final AtomicInteger parseCount = new AtomicInteger();
  private final ParsedFileType<String> parsedText = ParsedFileType.of(this::parse);

  @BeforeEach
  void init() throws IOException {
    appDir = tmpDir.toRealPath().resolve("app");
    FileUtils.forceMkdir(appDir.resolve("subdir").toFile());
    analyzedFile = appDir.resolve("Dockerfile");
    Files.writeString(appDir.resolve("requirements.txt"), "flask==3.1.0");
  }

  @Test
  void shouldReuseParsedResultForTheSameCanonicalFile() {
    assertThat(cache.readReferencedFileRelativeTo(analyzedFile, "requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(cache.readReferencedFileRelativeTo(analyzedFile, "subdir/../requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(parseCount).hasValue(1);
  }

  @Test
  void shouldKeepParserTypesSeparate() {
    var parsedLength = ParsedFileType.of(String::length);

    assertThat(cache.readReferencedFileRelativeTo(analyzedFile, "requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(cache.readReferencedFileRelativeTo(analyzedFile, "requirements.txt", parsedLength)).contains(12);
    assertThat(parseCount).hasValue(1);
  }

  @Test
  void shouldReturnEmptyForMissingFilesWithoutParsing() {
    assertThat(cache.readReferencedFileRelativeTo(analyzedFile, "missing.txt", parsedText)).isEmpty();
    assertThat(parseCount).hasValue(0);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "./subdir/../requirements.txt",
    "subdir/./nested/../../requirements.txt",
    "../app/./subdir/../requirements.txt",
  })
  void shouldResolveTestHelperCanonicalPathForRelativeReferences(String referencedPath) {
    assertThat(TestFileSystemUtils.resolveReferencedFileRelativeTo(analyzedFile, referencedPath))
      .hasValueSatisfying(file -> {
        assertThat(file.canonicalPath()).isEqualTo(appDir.resolve("requirements.txt"));
        assertThat(file.contents()).hasValue("flask==3.1.0");
      });
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "getCanonicalFile() does not dereference symlinks/junctions on Windows, a known and accepted gap")
  void shouldRejectTestHelperSymlinkLeavingAnalyzedDirectory() throws IOException {
    Files.writeString(appDir.getParent().resolve("secret.txt"), "secret");
    symlink(appDir.resolve("outside"), appDir.getParent());

    assertThat(TestFileSystemUtils.resolveReferencedFileRelativeTo(analyzedFile, "outside/secret.txt")).isEmpty();
  }

  private String parse(String content) {
    parseCount.incrementAndGet();
    return "parsed: " + content;
  }

  private static Path symlink(Path link, Path target) {
    try {
      return Files.createSymbolicLink(link, target);
    } catch (IOException | UnsupportedOperationException e) {
      return abort("Cannot create a symbolic link on this platform: " + e);
    }
  }
}
