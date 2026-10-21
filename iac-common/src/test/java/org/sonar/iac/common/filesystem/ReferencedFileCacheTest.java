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

import com.sonarsource.scanner.engine.sensor.test.fixtures.SensorContextTester;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.event.Level;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class ReferencedFileCacheTest {

  @RegisterExtension
  public final LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @TempDir
  private Path tmpDir;

  private Path baseDir;
  private SensorContextTester context;
  private final ReferencedFileCache cache = new ReferencedFileCache();
  private final AtomicInteger parseCount = new AtomicInteger();
  private final ParsedFileType<String> parsedText = ParsedFileType.of(this::parse);
  private final ParsedFileType<Boolean> parsedBoolean = ParsedFileType.of(content -> true);
  private final ParsedFileType<ParsedConfig> parsedConfig = ParsedFileType.of(this::parseConfig);

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toRealPath().resolve("test-project");
    FileUtils.forceMkdir(baseDir.toFile());
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void shouldCacheMissingReferences() {
    assertThat(cache.read(context, baseDir, "missing.txt", parsedText)).isEmpty();
    context.fileSystem().add(inputFile("missing.txt", baseDir, "now indexed", null));

    assertThat(cache.read(context, baseDir, "missing.txt", parsedText)).isEmpty();
    assertThat(new ReferencedFileCache().read(context, baseDir, "missing.txt", parsedText)).contains("parsed: now indexed");
  }

  @Test
  void shouldParseTheSameCanonicalFileOnlyOnce() throws IOException {
    var indexed = spy(inputFile("requirements.txt", baseDir, "flask==3.1.0", null));
    context.fileSystem().add(indexed);

    assertThat(cache.read(context, baseDir, "requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(cache.read(context, baseDir, ".github/../requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(parseCount).hasValue(1);
    verify(indexed, times(1)).contents();
  }

  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "getCanonicalFile() does not dereference symlinks/junctions on Windows, a known and accepted gap")
  void shouldShareParsedResultAcrossSymlinkedFilePath() throws IOException {
    Files.writeString(baseDir.resolve("requirements.txt"), "flask==3.1.0");
    var indexed = spy(inputFile("requirements.txt", baseDir, "flask==3.1.0", null));
    context.fileSystem().add(indexed);
    symlink(baseDir.resolve("requirements-link.txt"), baseDir.resolve("requirements.txt"));

    assertThat(cache.read(context, baseDir, "requirements-link.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(cache.read(context, baseDir, "requirements.txt", parsedText)).contains("parsed: flask==3.1.0");
    assertThat(parseCount).hasValue(1);
    verify(indexed, times(1)).contents();
  }

  @Test
  void shouldKeepDifferentFilesAndTypesSeparate() {
    context.fileSystem().add(inputFile("first.txt", baseDir, "same", null));
    context.fileSystem().add(inputFile("second.txt", baseDir, "same", null));

    assertThat(cache.read(context, baseDir, "first.txt", parsedText)).contains("parsed: same");
    assertThat(cache.read(context, baseDir, "second.txt", parsedText)).contains("parsed: same");
    assertThat(cache.read(context, baseDir, "first.txt", parsedBoolean)).contains(true);
    assertThat(parseCount).hasValue(2);
  }

  @Test
  void shouldCacheBestEffortParsedResultForMalformedContent() {
    context.fileSystem().add(inputFile("config.txt", baseDir, "malformed", null));

    assertThat(cache.read(context, baseDir, "config.txt", parsedConfig)).contains(new ParsedConfig(false));
    assertThat(cache.read(context, baseDir, "config.txt", parsedConfig)).contains(new ParsedConfig(false));
    assertThat(parseCount).hasValue(1);
  }

  @Test
  void shouldLogThePathAndCacheTheEmptyResultWhenParsingThrows() {
    context.fileSystem().add(inputFile("config.txt", baseDir, "malformed", null));
    var failingType = ParsedFileType.<String>of(content -> {
      parseCount.incrementAndGet();
      throw new IllegalStateException("broken parser");
    });

    assertThat(cache.read(context, baseDir, "config.txt", failingType)).isEmpty();
    assertThat(cache.read(context, baseDir, "config.txt", failingType)).isEmpty();

    assertThat(parseCount).hasValue(1);
    assertThat(logTester.logs(Level.DEBUG)).containsExactly("Could not parse referenced file " + baseDir.resolve("config.txt"));
  }

  private String parse(String content) {
    parseCount.incrementAndGet();
    return "parsed: " + content;
  }

  private ParsedConfig parseConfig(String content) {
    parseCount.incrementAndGet();
    return new ParsedConfig(content.startsWith("valid"));
  }

  private record ParsedConfig(boolean valid) {
  }

  private static Path symlink(Path link, Path target) {
    try {
      return Files.createSymbolicLink(link, target);
    } catch (IOException | UnsupportedOperationException e) {
      return abort("Cannot create a symbolic link on this platform: " + e);
    }
  }
}
