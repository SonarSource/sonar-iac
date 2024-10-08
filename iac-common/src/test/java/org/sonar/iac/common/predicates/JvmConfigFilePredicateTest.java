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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.JVM_CONFIG_FILE_PATTERNS_KEY;
import static org.sonar.iac.common.predicates.JvmConfigFilePredicate.getFilePatterns;

class JvmConfigFilePredicateTest {

  @TempDir
  private Path tempDir;

  @ParameterizedTest
  @CsvSource(textBlock = """
    application.properties,true
    application.yaml,true
    application.yml,true
    application-prod.properties,true
    application-prod.yaml,true
    application-prod.yml,true
    application-dev.properties,false
    application-dev.yaml,false
    application-dev.yml,false
    application-test.properties,false
    application-test.yaml,false
    application-test.yml,false
    config.properties,false
    config.yaml,false
    config.yml,false
    """)
  void shouldCorrectlyMatchFiles(String filename, boolean shouldMatch) {
    var predicate = new JvmConfigFilePredicate(SensorContextTester.create(tempDir), true);
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
