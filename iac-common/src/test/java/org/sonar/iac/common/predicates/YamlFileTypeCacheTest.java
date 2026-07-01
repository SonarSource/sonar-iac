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
import org.junit.jupiter.api.Test;
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
  void shouldExposeFilesGroupedByType() {
    var cache = new YamlFileTypeCache();
    var cloudFormation1 = inputFile("file:///cf1.yaml");
    var cloudFormation2 = inputFile("file:///cf2.yaml");
    var kubernetes = inputFile("file:///k8s.yaml");
    cache.put(cloudFormation1, FileType.CLOUDFORMATION);
    cache.put(cloudFormation2, FileType.CLOUDFORMATION);
    cache.put(kubernetes, FileType.KUBERNETES);

    assertThat(cache.getFiles(FileType.CLOUDFORMATION)).containsExactlyInAnyOrder(cloudFormation1, cloudFormation2);
    assertThat(cache.getFiles(FileType.KUBERNETES)).containsExactly(kubernetes);
    assertThat(cache.getFiles(FileType.CLOUDFORMATION, FileType.KUBERNETES))
      .containsExactlyInAnyOrder(cloudFormation1, cloudFormation2, kubernetes);
  }

  @Test
  void shouldReturnEmptyForTypeWithoutFiles() {
    var cache = new YamlFileTypeCache();
    assertThat(cache.getFiles(FileType.CLOUDFORMATION)).isEmpty();
  }

  @Test
  void shouldNotIndexUndeterminedFiles() {
    var cache = new YamlFileTypeCache();
    var file = inputFile("file:///plain.yaml");
    cache.put(file, FileType.UNDETERMINED);

    assertThat(cache.get(file.uri())).isEqualTo(FileType.UNDETERMINED);
    assertThat(cache.getFiles(FileType.UNDETERMINED)).isEmpty();
  }

  @Test
  void shouldMoveFileToNewBucketWhenReclassified() {
    var cache = new YamlFileTypeCache();
    var file = inputFile("file:///changing.yaml");
    cache.put(file, FileType.CLOUDFORMATION);
    cache.put(file, FileType.KUBERNETES);

    assertThat(cache.getFiles(FileType.CLOUDFORMATION)).isEmpty();
    assertThat(cache.getFiles(FileType.KUBERNETES)).containsExactly(file);
  }

  private static InputFile inputFile(String uri) {
    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(URI.create(uri));
    return inputFile;
  }
}
