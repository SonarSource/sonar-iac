/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.docker.plugin;

import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.internal.PathPattern;

import static org.assertj.core.api.Assertions.assertThat;

class DockerLanguageTest {

  @Test
  void shouldReturnDockerFileSuffixes() {
    DockerLanguage language = new DockerLanguage();
    assertThat(language.getFileSuffixes()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Dockerfile",
    "filename.dockerfile",
    "filename.Dockerfile",
    "filename.dOckerFilE"
  })
  void fileNameIsAssignedToLanguage(String fileName) {
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
    // TODO: SONARIAC-1095 Extend DockerLanguage to include possible conflicting filePatterns
    "Dockerfile.foo"
  })
  void fileNameIsNotAssignedToLanguage(String fileName) {
    assertThat(associatedToLanguage(fileName)).isFalse();
  }

  private static boolean associatedToLanguage(String fileName) {
    DockerLanguage language = new DockerLanguage();

    // Based on the actual implementation in SQ and SC
    Path realAbsolutePath = Path.of("src", "main", "resources", fileName).toAbsolutePath().normalize();
    Path projectRelativePath = Path.of("").toAbsolutePath().relativize(realAbsolutePath);
    return Arrays.stream(language.filenamePatterns())
      .map(filenamePattern -> "**/" + filenamePattern)
      .map(PathPattern::create)
      .anyMatch(pattern -> pattern.match(realAbsolutePath, projectRelativePath, false));
  }

}
