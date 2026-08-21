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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.languages.IacLanguage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.filesystem.FileSystemUtils.canonical;
import static org.sonar.iac.common.filesystem.FileSystemUtils.directoryOf;
import static org.sonar.iac.common.filesystem.FileSystemUtils.readReferencedFile;
import static org.sonar.iac.common.filesystem.FileSystemUtils.retrieveHelmProjectFolder;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.inputFile;

class FileSystemUtilsTest {

  @TempDir
  protected File tmpDir;

  private Path baseDir;

  private SensorContextTester context;

  @BeforeEach
  void init() throws IOException {
    baseDir = tmpDir.toPath().toRealPath().resolve("test-project");
    FileUtils.forceMkdir(baseDir.toFile());
    context = SensorContextTester.create(baseDir);
  }

  @Test
  void shouldReturnNullWhenInputIsNull() {
    var parentPath = retrieveHelmProjectFolder(null, context.fileSystem());
    assertThat(parentPath).isNull();
  }

  @Test
  void shouldReturnNullIfParentIsNull() throws IOException {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(null);
      var canonicalFile = mock(File.class);
      when(inputFilePath.toFile()).thenReturn(canonicalFile);
      when(canonicalFile.getCanonicalFile()).thenReturn(canonicalFile);
      when(canonicalFile.toPath()).thenReturn(inputFilePath);

      var parentPath = retrieveHelmProjectFolder(inputFilePath, context.fileSystem());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnNullIfParentIsNotNullAndDirectoryIsIncorrect() throws IOException {
    try (var ignored = Mockito.mockStatic(Files.class)) {
      when(Files.exists(any())).thenReturn(false);

      var inputFilePath = mock(Path.class);
      when(inputFilePath.getParent()).thenReturn(mock(Path.class));
      var canonicalFile = mock(File.class);
      when(inputFilePath.toFile()).thenReturn(canonicalFile);
      when(canonicalFile.getCanonicalFile()).thenReturn(canonicalFile);
      when(canonicalFile.toPath()).thenReturn(inputFilePath);

      var parentPath = retrieveHelmProjectFolder(inputFilePath, context.fileSystem());
      assertThat(parentPath).isNull();
    }
  }

  @Test
  void shouldReturnNullWhenOnlyChartYamlIsVeryHighAbove() throws IOException {
    Files.createFile(tmpDir.toPath().toRealPath().resolve("Chart.yaml"));
    FileUtils.forceMkdir(baseDir.resolve("templates/sub1/sub2/sub3/sub4").toFile());
    var helmTemplate = inputFile("templates/sub1/sub2/sub3/sub4/pod.yaml", baseDir, "", "kubernetes");
    var templateInputFileContext = new InputFileContext(context, helmTemplate, IacLanguage.KUBERNETES);

    var result = retrieveHelmProjectFolder(Path.of(templateInputFileContext.inputFile.uri()), context.fileSystem());

    assertThat(result).isNull();
  }

  @Test
  void shouldHandleIOExceptionWhenResolvingCanonicalPath() throws IOException {
    Files.createFile(baseDir.resolve("Chart.yaml"));
    Files.createFile(baseDir.resolve("test.yaml"));
    var inputFilePath = baseDir.resolve("test.yaml");

    // Layer 1: Mock File that throws IOException on getCanonicalFile()
    var mockFileFromToFile = Mockito.mock(java.io.File.class);
    when(mockFileFromToFile.getCanonicalFile()).thenThrow(new IOException("Test IOException in canonical path resolution"));

    // Layer 2: Mock Path that returns the mock File from Layer 1 on toFile()
    var mockPathFromBaseDir = Mockito.spy(baseDir.resolve(""));
    when(mockPathFromBaseDir.toFile()).thenReturn(mockFileFromToFile);

    // Layer 3: Mock File that returns the mock Path from Layer 2 on toPath()
    var mockBaseDirFile = Mockito.mock(java.io.File.class);
    when(mockBaseDirFile.toPath()).thenReturn(mockPathFromBaseDir);

    // Layer 4: Mock FileSystem that returns the mock File from Layer 3 on baseDir()
    var mockFileSystem = Mockito.mock(org.sonar.api.batch.fs.FileSystem.class);
    when(mockFileSystem.baseDir()).thenReturn(mockBaseDirFile);

    var result = FileSystemUtils.retrieveHelmProjectFolder(
      inputFilePath,
      mockFileSystem,
      path -> Files.exists(path) && Files.isRegularFile(path) && path.getFileName().toString().equals("Chart.yaml"));
    assertThat(result).isEqualTo(baseDir);
  }

  @Test
  void shouldHandleUnsupportedOperationExceptionWhenResolvingCanonicalPath() throws IOException {
    Files.createFile(baseDir.resolve("Chart.yaml"));
    var inputFilePath = spy(baseDir.resolve("test.yaml"));
    // toFile() rejects paths outside the default file system
    when(inputFilePath.toFile()).thenThrow(new UnsupportedOperationException("not a default file system path"));

    var result = retrieveHelmProjectFolder(inputFilePath, context.fileSystem(), Files::exists);

    assertThat(result).isEqualTo(baseDir);
  }

  @Test
  void canonicalShouldKeepDescendantConsistentWithAncestorWhenNeitherExistsOnDisk() {
    var missingDir = baseDir.resolve("missing");
    var missingDescendant = missingDir.resolve("deeper/file.txt");

    assertThat(canonical(missingDescendant)).isEqualTo(canonical(missingDir).resolve("deeper").resolve("file.txt"));
  }

  /**
   * Pins the mechanism the previous test relies on: only the deepest existing ancestor is ever canonicalized.
   * Canonicalizing the full descendant path directly is what made two related paths resolve inconsistently on Windows.
   */
  @Test
  void canonicalShouldNotCanonicalizeSegmentsThatDoNotExistOnDisk() {
    var missingChild = mock(Path.class);
    var missingLeaf = mock(Path.class);
    when(missingLeaf.toAbsolutePath()).thenReturn(missingLeaf);
    when(missingLeaf.normalize()).thenReturn(missingLeaf);
    when(missingLeaf.getFileName()).thenReturn(Path.of("file.txt"));
    when(missingLeaf.getParent()).thenReturn(missingChild);
    when(missingChild.getFileName()).thenReturn(Path.of("missing"));
    when(missingChild.getParent()).thenReturn(baseDir);

    try (var filesMock = Mockito.mockStatic(Files.class, Mockito.CALLS_REAL_METHODS)) {
      filesMock.when(() -> Files.exists(missingLeaf)).thenReturn(false);
      filesMock.when(() -> Files.exists(missingChild)).thenReturn(false);

      var result = canonical(missingLeaf);

      assertThat(result).isEqualTo(canonical(baseDir).resolve("missing").resolve("file.txt"));
      verify(missingLeaf, never()).toFile();
      verify(missingChild, never()).toFile();
    }
  }

  @Test
  void shouldReadReferencedFileRelativeToAnalyzedFileDirectory() {
    indexFile("app/requirements.txt", "flask==3.1.0 --hash=sha256:abc");
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements.txt"))
      .hasValue("flask==3.1.0 --hash=sha256:abc");
  }

  @Test
  void shouldReadReferencedFileRelativeToProjectRoot() {
    indexFile("requirements.txt", "flask==3.1.0 --hash=sha256:abc");

    assertThat(readReferencedFile(context, baseDir, ".github/../requirements.txt"))
      .hasValue("flask==3.1.0 --hash=sha256:abc");
  }

  /**
   * The scanner indexes files under the base directory it was pointed at, without resolving symlinks, so a lookup by the
   * canonical path finds nothing whenever the project root is reached through one: macOS temporary directories under
   * {@code /var}, a symlinked CI workspace, or (with the short and long form swapped) a Windows 8.3 base directory.
   */
  @Test
  void shouldReadReferencedFileWhenProjectRootIsReachedThroughSymlink() throws IOException {
    var symlinkedBaseDir = symlink(baseDir.getParent().resolve("symlinked-project"), baseDir);
    var symlinkedContext = SensorContextTester.create(symlinkedBaseDir);
    symlinkedContext.fileSystem().add(inputFile("app/requirements.txt", symlinkedBaseDir, "flask==3.1.0 --hash=sha256:abc", null));
    var analyzedFile = inputFile("app/Dockerfile", symlinkedBaseDir, "FROM python", null);

    assertThat(readReferencedFile(symlinkedContext, directoryOf(analyzedFile), "requirements.txt"))
      .hasValue("flask==3.1.0 --hash=sha256:abc");
  }

  /**
   * The other way around: the file is indexed under its real path while the reference is resolved through a symlink, which
   * the canonical fallback covers.
   */
  @Test
  @DisabledOnOs(value = OS.WINDOWS, disabledReason = "getCanonicalFile() does not dereference symlinks/junctions on Windows, a known and accepted gap")
  void shouldReadReferencedFileIndexedUnderItsRealPathWhenReferencedThroughSymlink() throws IOException {
    indexFile("app/requirements.txt", "flask==3.1.0 --hash=sha256:abc");
    var symlinkedBaseDir = symlink(baseDir.getParent().resolve("symlinked-project"), baseDir);
    var analyzedFile = inputFile("app/Dockerfile", symlinkedBaseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements.txt"))
      .hasValue("flask==3.1.0 --hash=sha256:abc");
  }

  @Test
  void shouldNotReadReferencedFileLeavingWorkingDirectoryThroughSymlink() throws IOException {
    context.fileSystem().add(inputFile("secret.txt", baseDir.getParent(), "flask==3.1.0 --hash=sha256:abc", null));
    // The link sits inside the working directory but points outside of it
    symlink(baseDir.resolve("outside"), baseDir.getParent());
    var analyzedFile = inputFile("Dockerfile", baseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "outside/secret.txt")).isEmpty();
  }

  private static Path symlink(Path link, Path target) {
    try {
      return Files.createSymbolicLink(link, target);
    } catch (IOException | UnsupportedOperationException e) {
      return abort("Cannot create a symbolic link on this platform: " + e);
    }
  }

  @Test
  void shouldNotReadReferencedFileInSonarLintContext() {
    indexFile("app/requirements.txt", "flask==3.1.0 --hash=sha256:abc");
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);
    var sensorContext = mock(SensorContext.class);
    var fileSystem = mock(FileSystem.class);
    when(sensorContext.runtime()).thenReturn(SONARLINT_RUNTIME_9_9);
    when(sensorContext.fileSystem()).thenReturn(fileSystem);
    // SonarLint does not reliably index the whole project for a single-file analysis; simulate that querying it blows up,
    // and assert readReferencedFile never reaches that call in this context.
    when(fileSystem.inputFile(any())).thenThrow(new IllegalStateException("must not be called in SonarLint context"));

    assertThat(readReferencedFile(sensorContext, directoryOf(analyzedFile), "requirements.txt")).isEmpty();
  }

  @Test
  void shouldNotReadSameNamedFileFromSiblingDirectory() {
    indexFile("services/b/requirements.txt", "flask==3.1.0 --hash=sha256:abc");
    var analyzedFile = inputFile("services/a/Dockerfile", baseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements.txt")).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenReferencedFileIsNotFound() {
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements.txt")).isEmpty();
  }

  @Test
  void shouldNotReadReferencedFileOutsideWorkingDirectory() throws IOException {
    var secretFile = inputFile("secret.txt", tmpDir.toPath().toRealPath(), "flask==3.1.0 --hash=sha256:abc", null);
    context.fileSystem().add(secretFile);
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "../../secret.txt")).isEmpty();
  }

  @Test
  void shouldReturnEmptyForNonLocalReferences() {
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);
    var analyzedDir = directoryOf(analyzedFile);

    assertThat(readReferencedFile(context, analyzedDir, "/etc/passwd")).isEmpty();
    assertThat(readReferencedFile(context, analyzedDir, "https://example.com/requirements.txt")).isEmpty();
    assertThat(readReferencedFile(context, analyzedDir, "$REQUIREMENTS")).isEmpty();
    assertThat(readReferencedFile(context, analyzedDir, "~/requirements.txt")).isEmpty();
  }

  @Test
  void shouldReturnEmptyForPathTheFileSystemRejects() {
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);

    // A NUL byte makes the path unparsable on any file system.
    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements\0.txt")).isEmpty();
  }

  @Test
  void shouldReturnEmptyWhenReferencedFileCannotBeRead() throws IOException {
    var analyzedFile = inputFile("app/Dockerfile", baseDir, "FROM python", null);
    var unreadableFile = spy(inputFile("app/requirements.txt", baseDir, "flask==3.1.0 --hash=sha256:abc", null));
    when(unreadableFile.contents()).thenThrow(new IOException("unreadable"));
    context.fileSystem().add(unreadableFile);

    assertThat(readReferencedFile(context, directoryOf(analyzedFile), "requirements.txt")).isEmpty();
  }

  @Test
  void shouldReadReferencedFileWhenWorkingDirectoryHasNoCanonicalForm() {
    indexFile("app/requirements.txt", "flask==3.1.0 --hash=sha256:abc");
    var workingDirectory = spy(baseDir.resolve("app"));
    // toFile() rejects paths outside the default file system
    when(workingDirectory.toFile()).thenThrow(new UnsupportedOperationException("not a default file system path"));

    assertThat(readReferencedFile(context, workingDirectory, "requirements.txt"))
      .hasValue("flask==3.1.0 --hash=sha256:abc");
  }

  @Test
  void shouldReturnEmptyWhenWorkingDirectoryIsUnknown() {
    assertThat(readReferencedFile(context, null, "requirements.txt")).isEmpty();
  }

  @Test
  void shouldNotResolveDirectoryOfNonFileSchemeUri() {
    var analyzedFile = mock(InputFile.class);
    when(analyzedFile.uri()).thenReturn(URI.create("jar:file:/project/app.jar!/Dockerfile"));

    assertThat(directoryOf(analyzedFile)).isNull();
  }

  private void indexFile(String relativePath, String content) {
    context.fileSystem().add(inputFile(relativePath, baseDir, content, null));
  }
}
