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
package org.sonar.iac.common.predicates;

import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;

class CloudFormationFilePredicateTest {
  @TempDir
  private Path tempDir;
  private SensorContext context;

  @BeforeEach
  void setUp() {
    var settings = new MapSettings();
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "AWSTemplateFormatVersion");
    context = SensorContextTester.create(tempDir).setSettings(settings);
  }

  @ParameterizedTest
  @MethodSource
  void shouldMatchCloudFormationFile(String content, boolean shouldMatch) {
    var predicate = new CloudFormationFilePredicate(context, true, new DurationStatistics(mock(Configuration.class)).timer("timer"));
    var matches = predicate.apply(IacTestUtils.inputFile("test.yaml", tempDir, content, "cloudformation"));
    assertThat(matches).isEqualTo(shouldMatch);
  }

  static Stream<Arguments> shouldMatchCloudFormationFile() {
    return Stream.of(
      of("AWSTemplateFormatVersion: X", true),
      of("apiVersion: v1", false));
  }
}
