/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.predicates;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.iac.common.extension.DurationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_KEY;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.getFilePatterns;

class JvmConfigFilePredicateTest {

  @TempDir
  private Path tempDir;

  static Stream<Arguments> shouldCorrectlyMatchFiles() {
    return Stream.of(
      arguments("application.properties", true),
      arguments("application.yaml", true),
      arguments("application.yml", true),
      arguments("application-prod.properties", true),
      arguments("application-prod.yaml", true),
      arguments("application-prod.yml", true),

      arguments("app.properties", true),
      arguments("app.yaml", true),
      arguments("app.yml", true),
      arguments("app-prod.properties", true),
      arguments("app-prod.yaml", true),
      arguments("app-prod.yml", true),

      arguments("foo-app.properties", true),
      arguments("foo-app.yaml", true),
      arguments("foo-app.yml", true),
      arguments("foo-app-prod.properties", true),
      arguments("foo-app-prod.yaml", true),
      arguments("foo-app-prod.yml", true),

      // dev profiles are ignored
      arguments("application-dev.properties", false),
      arguments("application-dev.yaml", false),
      arguments("application-dev.yml", false),
      // test profiles are ignored
      arguments("application-test.properties", false),
      arguments("application-test.yaml", false),
      arguments("application-test.yml", false),
      // doesn't contain app
      arguments("config.properties", false),
      arguments("config.yaml", false),
      arguments("config.yml", false));
  }

  @ParameterizedTest
  @MethodSource
  void shouldCorrectlyMatchFiles(String filename, boolean shouldMatch) {
    var predicate = new JvmConfigFilePredicate(SensorContextTester.create(tempDir), true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var inputFile = mock(InputFile.class);
    when(inputFile.filename()).thenReturn(filename);
    when(inputFile.relativePath()).thenReturn("src/main/resources/" + filename);

    assertThat(predicate.apply(inputFile)).isEqualTo(shouldMatch);
  }

  @ParameterizedTest
  @MethodSource
  void shouldUseDefaultFilePatternsIfProvidedAreEmpty(List<String> input) {
    var config = mock(Configuration.class);
    when(config.getStringArray(JVM_CONFIG_FILE_PATTERNS_KEY)).thenReturn(input.toArray(new String[0]));

    var patterns = getFilePatterns(config);

    assertThat(patterns).containsExactly(JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE.split(","));
  }

  static Stream<List<String>> shouldUseDefaultFilePatternsIfProvidedAreEmpty() {
    return Stream.of(
      List.of(""),
      List.of("", ""));
  }
}
