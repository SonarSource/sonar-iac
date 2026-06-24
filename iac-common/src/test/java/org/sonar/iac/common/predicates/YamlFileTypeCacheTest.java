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
import org.sonarsource.sonarlint.plugin.api.module.file.ModuleFileEvent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlFileTypeCacheTest {

  @Test
  void shouldStoreValueInCache() {
    YamlFileTypeCache cache = new YamlFileTypeCache();
    URI fileUri = URI.create("file:///test.yaml");
    FileType fileType = FileType.CLOUDFORMATION;
    cache.put(fileUri, fileType);
    assertThat(cache.get(fileUri)).isEqualTo(fileType);
  }

  @Test
  void shouldReturnNullWhenValueIsNotStored() {
    YamlFileTypeCache cache = new YamlFileTypeCache();
    URI fileUri = URI.create("file:///test.yaml");
    assertThat(cache.get(fileUri)).isNull();
  }

  @Test
  void shouldInvalidateCacheEntryOnModuleFileEvent() {
    YamlFileTypeCache cache = new YamlFileTypeCache();
    URI changedUri = URI.create("file:///changed.yaml");
    URI otherUri = URI.create("file:///other.yaml");
    cache.put(changedUri, FileType.CLOUDFORMATION);
    cache.put(otherUri, FileType.KUBERNETES);

    cache.process(moduleFileEvent(changedUri, ModuleFileEvent.Type.MODIFIED));

    assertThat(cache.get(changedUri)).isNull();
    // Other entries are untouched.
    assertThat(cache.get(otherUri)).isEqualTo(FileType.KUBERNETES);
  }

  @Test
  void shouldNotFailWhenInvalidatingUnknownFile() {
    YamlFileTypeCache cache = new YamlFileTypeCache();
    URI unknownUri = URI.create("file:///unknown.yaml");

    cache.process(moduleFileEvent(unknownUri, ModuleFileEvent.Type.DELETED));

    assertThat(cache.get(unknownUri)).isNull();
  }

  private static ModuleFileEvent moduleFileEvent(URI uri, ModuleFileEvent.Type type) {
    var inputFile = mock(InputFile.class);
    when(inputFile.uri()).thenReturn(uri);
    var event = mock(ModuleFileEvent.class);
    when(event.getTarget()).thenReturn(inputFile);
    when(event.getType()).thenReturn(type);
    return event;
  }
}
