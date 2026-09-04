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
package org.sonar.iac.docker.plugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.scanner.plugin.api.impl.config.MapSettings;
import org.sonar.scanner.plugin.api.impl.fs.PathPattern;

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
    assertThat(language.filenamePatterns()).containsExactly(DockerSettings.DEFAULT_FILE_PATTERNS.split(","));
  }

  @Test
  void shouldHaveDefaultFilenamePatternsWithEmptyProperty() {
    MapSettings settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, "");

    DockerLanguage language = new DockerLanguage(settings.asConfig());

    assertThat(language.filenamePatterns()).containsExactly(DockerSettings.DEFAULT_FILE_PATTERNS.split(","));
  }

  @ParameterizedTest(name = "[{index}] pattern=\"{0}\" -> isDefault={1}")
  @MethodSource("defaultFilePatternCases")
  void shouldDetermineIfDefaultFilePatternIsUsed(String pattern, boolean expected) {
    MapSettings settings = new MapSettings();
    settings.setProperty(DockerSettings.FILE_PATTERNS_KEY, pattern);
    DockerLanguage language = new DockerLanguage(settings.asConfig());
    assertThat(language.isUsingDefaultFilePattern()).isEqualTo(expected);
  }

  private static Stream<Arguments> defaultFilePatternCases() {
    return Stream.of(
      // the current defaults, verbatim
      Arguments.of(DockerSettings.DEFAULT_FILE_PATTERNS, true),
      // the current defaults, reordered
      Arguments.of("containerfile,dockerfile,*.dockerfile,Dockerfile,*.containerfile,Containerfile", true),
      // a narrowed subset of the defaults also counts as default
      Arguments.of("Dockerfile", true),
      // the pre-Containerfile default, which some projects may have persisted in their settings
      Arguments.of("*.dockerfile,Dockerfile,dockerfile", true),
      // one pattern outside the defaults means the user opted in
      Arguments.of("Dockerfile,*.foo", false),
      // an empty value falls back to the defaults
      Arguments.of("", true));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "dockerfile",
    "Dockerfile",
    "filename.dockerfile",
    "filename.Dockerfile",
    "filename.dOckerFilE",
    "containerfile",
    "Containerfile",
    "filename.containerfile",
    "filename.Containerfile",
    "filename.cOntainerFilE"
  })
  void fileNameShouldBeAssignedToLanguage(String fileName) {
    assertThat(associatedToLanguage(fileName)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "somefile",
    "FooDockerfile",
    "DockerfileFoo",
    "Dockerfile.java",
    "Helloworld.java",
    "Dockerfile.foo",
    "FooContainerfile",
    "ContainerfileFoo",
    "Containerfile.java",
    "Containerfile.foo"
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
