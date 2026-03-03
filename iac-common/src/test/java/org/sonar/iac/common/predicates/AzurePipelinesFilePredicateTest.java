/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AzurePipelinesFilePredicateTest {

  @TempDir
  Path tempDir;

  @ParameterizedTest
  @MethodSource
  void shouldDetectAzurePipelinesFile(String content, boolean expectedMatch) {
    var predicate = new AzurePipelinesFilePredicate(false, mock(DurationStatistics.Timer.class));
    assertThat(predicate.accept(IacTestUtils.inputFile("pipeline.yaml", tempDir, content, "yaml"))).isEqualTo(expectedMatch);
  }

  private static Stream<Arguments> shouldDetectAzurePipelinesFile() {
    return Stream.of(
      Arguments.of("""
        trigger:
          - main
        variables:
          myVar: value
        pool:
          vmImage: ubuntu-latest
        steps:
          - script: echo hello
        stages:
          - stage: test
        parameters:
          - name: env
        pr:
          branches:
            include: [main]
        resources:
          repositories: []
        """, true),
      Arguments.of("""
        steps:
        - script: echo
        """, true),
      Arguments.of("""
        trigger:
          - main
        """, true),
      Arguments.of("""
        nested:
          trigger:
            - main
          steps:
            - script: echo hello
        """, false),
      Arguments.of("", false));
  }
}
