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
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlFileTypeCacheTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  @Test
  void shouldStoreAndReturnFilesPerType() {
    var cache = new YamlFileTypeCache();
    var cloudFormationFile = inputFile("file:///cloudformation.yaml");
    var kubernetesFile = inputFile("file:///kubernetes.yaml");

    cache.putIfUncached(cloudFormationFile, FileType.CLOUDFORMATION);
    cache.putIfUncached(kubernetesFile, FileType.KUBERNETES);

    assertThat(cache.getInputFiles(Set.of(FileType.CLOUDFORMATION))).containsExactly(cloudFormationFile);
    assertThat(cache.getInputFiles(Set.of(FileType.KUBERNETES))).containsExactly(kubernetesFile);
  }

  @Test
  void shouldReturnEmptyListForATypeWithNoStoredFiles() {
    var cache = new YamlFileTypeCache();
    assertThat(cache.getInputFiles(Set.of(FileType.HELM))).isEmpty();
  }

  @Test
  void shouldReturnFilesOfAllRequestedTypesInFileTypeDeclarationOrder() {
    var cache = new YamlFileTypeCache();
    var cloudFormationFile = inputFile("file:///cloudformation.yaml");
    var kubernetesFile = inputFile("file:///kubernetes.yaml");
    var helmFile = inputFile("file:///helm.yaml");
    cache.putIfUncached(cloudFormationFile, FileType.CLOUDFORMATION);
    cache.putIfUncached(kubernetesFile, FileType.KUBERNETES);
    cache.putIfUncached(helmFile, FileType.HELM);

    assertThat(cache.getInputFiles(Set.of(FileType.KUBERNETES, FileType.CLOUDFORMATION)))
      .containsExactly(kubernetesFile, cloudFormationFile);
  }

  @Test
  void shouldReturnEmptyListWhenNoTypeIsRequested() {
    var cache = new YamlFileTypeCache();
    cache.putIfUncached(inputFile("file:///cloudformation.yaml"), FileType.CLOUDFORMATION);

    assertThat(cache.getInputFiles(Set.of())).isEmpty();
  }

  @Test
  void shouldPreserveInsertionOrderWithinAType() {
    var cache = new YamlFileTypeCache();
    var firstFile = inputFile("file:///first.yaml");
    var secondFile = inputFile("file:///second.yaml");
    cache.putIfUncached(firstFile, FileType.CLOUDFORMATION);
    cache.putIfUncached(secondFile, FileType.CLOUDFORMATION);

    assertThat(cache.getInputFiles(Set.of(FileType.CLOUDFORMATION))).containsExactly(firstFile, secondFile);
  }

  @Test
  void shouldSkipRequestedTypesWithNoStoredFiles() {
    var cache = new YamlFileTypeCache();
    var cloudFormationFile = inputFile("file:///cloudformation.yaml");
    cache.putIfUncached(cloudFormationFile, FileType.CLOUDFORMATION);

    assertThat(cache.getInputFiles(Set.of(FileType.HELM, FileType.CLOUDFORMATION))).containsExactly(cloudFormationFile);
  }

  @Test
  void shouldReturnACopyThatDoesNotAffectTheCache() {
    var cache = new YamlFileTypeCache();
    var cloudFormationFile = inputFile("file:///cloudformation.yaml");
    cache.putIfUncached(cloudFormationFile, FileType.CLOUDFORMATION);

    var result = cache.getInputFiles(Set.of(FileType.CLOUDFORMATION));
    result.clear();

    assertThat(cache.getInputFiles(Set.of(FileType.CLOUDFORMATION))).containsExactly(cloudFormationFile);
  }

  @Test
  void shouldTrackKnownFilesViaHasKnownType() {
    var cache = new YamlFileTypeCache();
    var knownFile = inputFile("file:///known.yaml");
    var unknownFile = inputFile("file:///unknown.yaml");
    cache.putIfUncached(knownFile, FileType.CLOUDFORMATION);

    assertThat(cache.hasKnownType(knownFile)).isTrue();
    assertThat(cache.hasKnownType(unknownFile)).isFalse();
  }

  @Test
  void shouldNotHaveCacheDataForAnyFileSystemInitially() {
    var cache = new YamlFileTypeCache();
    assertThat(cache.hasCacheDataFor(mock(FileSystem.class))).isFalse();
  }

  @Test
  void shouldBindToAFileSystemOnClearAndStartClassifyingFor() {
    var cache = new YamlFileTypeCache();
    var fileSystem = mock(FileSystem.class);

    cache.clearAndStartClassifyingFor(fileSystem);

    assertThat(cache.hasCacheDataFor(fileSystem)).isTrue();
  }

  @Test
  void shouldNotClearDataWhenRebindingToTheSameFileSystem() {
    var cache = new YamlFileTypeCache();
    var fileSystem = mock(FileSystem.class);
    var file = inputFile("file:///file.yaml");
    cache.clearAndStartClassifyingFor(fileSystem);
    cache.putIfUncached(file, FileType.CLOUDFORMATION);

    cache.clearAndStartClassifyingFor(fileSystem);

    assertThat(cache.getInputFiles(Set.of(FileType.CLOUDFORMATION))).containsExactly(file);
    assertThat(cache.hasKnownType(file)).isTrue();
  }

  @Test
  void shouldClearDataWhenRebindingToADifferentFileSystem() {
    // A multi-module analysis (sonar.modules) builds one file system per module while sharing this cache; moving to a
    // different module's file system must drop the previous module's data.
    var cache = new YamlFileTypeCache();
    var fileSystemA = mock(FileSystem.class);
    var fileSystemB = mock(FileSystem.class);
    var file = inputFile("file:///file.yaml");
    cache.clearAndStartClassifyingFor(fileSystemA);
    cache.putIfUncached(file, FileType.CLOUDFORMATION);

    cache.clearAndStartClassifyingFor(fileSystemB);

    assertThat(cache.hasCacheDataFor(fileSystemA)).isFalse();
    assertThat(cache.hasCacheDataFor(fileSystemB)).isTrue();
    assertThat(cache.getInputFiles(Set.of(FileType.CLOUDFORMATION))).isEmpty();
    assertThat(cache.hasKnownType(file)).isFalse();
  }

  @Test
  void shouldLogCountPerFileType() {
    var cache = new YamlFileTypeCache();
    cache.putIfUncached(inputFile("file:///cloudformation1.yaml"), FileType.CLOUDFORMATION);
    cache.putIfUncached(inputFile("file:///cloudformation2.yaml"), FileType.CLOUDFORMATION);
    cache.putIfUncached(inputFile("file:///kubernetes.yaml"), FileType.KUBERNETES);

    cache.logClassifiedCount();

    // Entries are ordered by FileType's declaration order (EnumMap), not insertion order - KUBERNETES is declared
    // before CLOUDFORMATION.
    assertThat(logTester.logs(Level.DEBUG)).containsExactly("Classified input files for: KUBERNETES: 1 file, CLOUDFORMATION: 2 files");
  }

  @Test
  void shouldUseSingularSuffixForASingleFile() {
    var cache = new YamlFileTypeCache();
    cache.putIfUncached(inputFile("file:///cloudformation.yaml"), FileType.CLOUDFORMATION);

    cache.logClassifiedCount();

    assertThat(logTester.logs(Level.DEBUG)).containsExactly("Classified input files for: CLOUDFORMATION: 1 file");
  }

  @Test
  void shouldLogAnEmptySummaryWhenNothingWasClassified() {
    var cache = new YamlFileTypeCache();

    cache.logClassifiedCount();

    assertThat(logTester.logs(Level.DEBUG)).containsExactly("Classified input files for: ");
  }

  @Test
  void shouldNotLogWhenDebugIsDisabled() {
    logTester.setLevel(Level.INFO);
    var cache = new YamlFileTypeCache();
    cache.putIfUncached(inputFile("file:///cloudformation.yaml"), FileType.CLOUDFORMATION);

    cache.logClassifiedCount();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
  }

  private static InputFile inputFile(String uri) {
    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(URI.create(uri));
    return inputFile;
  }
}
