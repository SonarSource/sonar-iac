/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.yaml;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class YamlLanguageTest {

  @Test
  void shouldReturnYamlFileSuffixes() {
    MapSettings settings = new MapSettings();
    YamlLanguage language = new YamlLanguage(settings.asConfig());
    assertThat(language.getFileSuffixes()).containsExactly(".yaml", ".yml");

    settings.setProperty(YamlLanguage.FILE_SUFFIXES_KEY, "");
    assertThat(language.getFileSuffixes()).containsExactly(".yaml", ".yml");

    settings.setProperty(YamlLanguage.FILE_SUFFIXES_KEY, ".bar, .foo");
    assertThat(language.getFileSuffixes()).containsOnly(".bar", ".foo");

    settings.setProperty(YamlLanguage.FILE_SUFFIXES_KEY, ".foo, , ");
    assertThat(language.getFileSuffixes()).containsOnly(".foo");
  }

  @Test
  void shouldNotPublishAllFiles() {
    MapSettings settings = new MapSettings();
    YamlLanguage language = new YamlLanguage(settings.asConfig());

    assertThat(language.publishAllFiles()).isFalse();
  }

  @Test
  void propertyShouldBeBuild() {
    assertThat(YamlLanguage.getProperty()).isNotNull();
  }

}
