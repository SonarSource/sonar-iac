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
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlFileTypeCacheTest {

  @Test
  void shouldStoreValueInCache() {
    var cache = new YamlFileTypeCache();
    var file = inputFile("file:///test.yaml");
    cache.put(file, FileType.CLOUDFORMATION);
    assertThat(cache.get(file.uri())).isEqualTo(FileType.CLOUDFORMATION);
  }

  @Test
  void shouldReturnNullWhenValueIsNotStored() {
    var cache = new YamlFileTypeCache();
    assertThat(cache.get(URI.create("file:///test.yaml"))).isNull();
  }

  @Test
  void shouldOverwriteTypeWhenReclassified() {
    var cache = new YamlFileTypeCache();
    var file = inputFile("file:///changing.yaml");
    cache.put(file, FileType.CLOUDFORMATION);
    cache.put(file, FileType.KUBERNETES);

    assertThat(cache.get(file.uri())).isEqualTo(FileType.KUBERNETES);
  }

  @Test
  void shouldReturnNullForClassifiedCandidatesOfUnknownFileSystem() {
    var cache = new YamlFileTypeCache();
    assertThat(cache.getClassifiedCandidates(mock(FileSystem.class))).isNull();
  }

  @Test
  void shouldMemoizeClassifiedCandidatesPerFileSystem() {
    var cache = new YamlFileTypeCache();
    var fileSystemA = mock(FileSystem.class);
    var fileSystemB = mock(FileSystem.class);
    var a1 = inputFile("file:///a/1.yaml");
    var a2 = inputFile("file:///a/2.yaml");
    var b1 = inputFile("file:///b/1.yaml");

    cache.putClassifiedCandidates(fileSystemA, List.of(a1, a2));
    cache.putClassifiedCandidates(fileSystemB, List.of(b1));

    // Order is preserved and each file system gets back only its own files (a multi-module analysis shares one cache).
    assertThat(cache.getClassifiedCandidates(fileSystemA)).containsExactly(a1, a2);
    assertThat(cache.getClassifiedCandidates(fileSystemB)).containsExactly(b1);
  }

  @Test
  void shouldTreatEmptyClassifiedCandidatesAsAHit() {
    var cache = new YamlFileTypeCache();
    var fileSystem = mock(FileSystem.class);
    cache.putClassifiedCandidates(fileSystem, List.of());

    // An empty (non-null) list is a cache hit: the file system was classified and simply has no candidate file.
    assertThat(cache.getClassifiedCandidates(fileSystem)).isNotNull().isEmpty();
  }

  private static InputFile inputFile(String uri) {
    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(URI.create(uri));
    return inputFile;
  }
}
