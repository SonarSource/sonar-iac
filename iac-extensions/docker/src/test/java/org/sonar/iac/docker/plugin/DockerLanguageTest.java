/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.plugin;

import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.internal.PathPattern;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class DockerLanguageTest {

  @Test
  void shouldReturnDockerFileSuffixes() {
    DockerLanguage language = new DockerLanguage(new MapSettings().asConfig());
    assertThat(language.getFileSuffixes()).isEmpty();
  }

  @Test
  void shouldCorrectlySanitizeCustomFilenamePatterns() {
    MapSettings settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, " *.ext , Dockerfile, , dockerfile.*");

    DockerLanguage language = new DockerLanguage(settings.asConfig());
    assertThat(language.filenamePatterns()).hasSize(3);
  }

  @Test
  void shouldHaveDefaultFilenamePatternsWithNoProvidedProperty() {
    DockerLanguage language = new DockerLanguage(new MapSettings().asConfig());
    assertThat(language.filenamePatterns()).hasSize(2);
    assertThat(language.filenamePatterns()).containsExactly(DockerSettings.DEFAULT_FILE_PATTERNS.split(","));
  }

  @Test
  void shouldHaveDefaultFilenamePatternsWithEmptyProperty() {
    MapSettings settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, "");

    DockerLanguage language = new DockerLanguage(settings.asConfig());

    assertThat(language.filenamePatterns()).hasSize(2);
    assertThat(language.filenamePatterns()).containsExactly(DockerSettings.DEFAULT_FILE_PATTERNS.split(","));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Dockerfile",
    "filename.dockerfile",
    "filename.Dockerfile",
    "filename.dOckerFilE"
  })
  void fileNameShouldBeAssignedToLanguage(String fileName) {
    assertThat(associatedToLanguage(fileName)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "dockerfile",
    "somefile",
    "FooDockerfile",
    "DockerfileFoo",
    "Dockerfile.java",
    "Helloworld.java",
    "Dockerfile.foo"
  })
  void fileNameShouldNotBeAssignedToLanguage(String fileName) {
    assertThat(associatedToLanguage(fileName)).isFalse();
  }

  private static boolean associatedToLanguage(String fileName) {
    DockerLanguage language = new DockerLanguage(new MapSettings().asConfig());

    // Based on 'LanguageDetection.getLanguagePatterns(...)' from SQ and SC
    Path realAbsolutePath = Path.of("src", "main", "resources", fileName).toAbsolutePath().normalize();
    Path projectRelativePath = Path.of("").toAbsolutePath().relativize(realAbsolutePath);
    return Arrays.stream(language.filenamePatterns())
      .map(filenamePattern -> "**/" + filenamePattern)
      .map(PathPattern::create)
      .anyMatch(pattern -> pattern.match(realAbsolutePath, projectRelativePath, false));
  }

}
