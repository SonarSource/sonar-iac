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

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.extension.DurationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.predicates.FilePredicateTestUtils.newInputFileMock;

class GithubActionsFilePredicateTest {

  @TempDir
  Path tempDir;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private GithubActionsFilePredicate predicateNoLog;

  @BeforeEach
  void setUp() {
    var sensorContext = SensorContextTester.create(tempDir);
    predicateNoLog = new GithubActionsFilePredicate(sensorContext.fileSystem().predicates(), false, new DurationStatistics(mock(Configuration.class)).timer("test"));
  }

  private static final String VALID_FULL_ACTION_CONTENT = """
    name: Deploy
    description: Deploys the application
    runs:
      using: 'composite'
      steps:
        - name: Checkout code
          uses: actions/checkout@v2
    """;

  @ParameterizedTest
  @ValueSource(strings = {
    ".github/workflows/deploy.yaml",
    ".github/workflows/deploy.yml",
    "/full/absolute/path/.github/workflows/deploy.yaml",
    "./relative/path/.github/workflows/deploy.yaml",
    "relative/path/.github/workflows/deploy.yaml",
  })
  void shouldMatchWorkflowFile(String filePath) throws IOException {
    var inputFile = newInputFileMock(filePath, "");
    assertThat(predicateNoLog.apply(inputFile)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "action.yaml",
    "somewhere/action.yaml",
    ".github/workflows/action.yaml",
  })
  void shouldMatchActionFileValidNameAndAllIdentifiers(String filePath) throws IOException {
    var inputFile = newInputFileMock(filePath, VALID_FULL_ACTION_CONTENT);
    assertThat(predicateNoLog.apply(inputFile)).isTrue();
  }

  @Test
  void shouldMatchActionFileWithJustEnoughIdentifiers() throws IOException {
    var inputFile = newInputFileMock("action.yaml", """
      name: Deploy
      description: Deploys the application
      runs:""");
    assertThat(predicateNoLog.apply(inputFile)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    ".github/deploy.yaml",
    ".other/workflows/deploy.yaml",
    ".github/middle/workflows/deploy.yaml",
    "deploy.yml",
    ".github/workflows/with/subfolders/deploy.yaml",
    ".github/workflows/other.txt",
  })
  void shouldNotMatchFileWithImproperWorkflowPath(String filePath) throws IOException {
    var inputFile = newInputFileMock(filePath, "");
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "actions.yaml",
    "other.yaml",
    "action/other.yaml",
    "action.json",
    "action",
  })
  void shouldNotMatchActionFileWithInvalidName(String filePath) throws IOException {
    var inputFile = newInputFileMock(filePath, VALID_FULL_ACTION_CONTENT);
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @Test
  void shouldNotMatchActionFileWithEmptyContent() throws IOException {
    var inputFile = newInputFileMock("action.yaml", "");
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @Test
  void shouldNotMatchActionFileWithContentContainingNoIdentifiers() throws IOException {
    var inputFile = newInputFileMock("action.yaml", """
      unrelated: content""");
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @Test
  void shouldNotMatchActionFileWithContentContainingNotEnoughIdentifiers() throws IOException {
    var inputFile = newInputFileMock("action.yaml", """
      name: Deploy
      description: Deploys the application""");
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @Test
  void shouldNotMatchActionFileWithIdentifiersAfterTooBigContent() throws IOException {
    var inputFile = newInputFileMock("action.yaml", "a".repeat(8200) + VALID_FULL_ACTION_CONTENT);
    assertThat(predicateNoLog.apply(inputFile)).isFalse();
  }

  @Test
  void shouldLogDebugMessageWhenEnabled() throws IOException {
    var sensorContext = SensorContextTester.create(tempDir);
    var predicateWithLog = new GithubActionsFilePredicate(sensorContext.fileSystem().predicates(), true, new DurationStatistics(mock(Configuration.class)).timer("test"));
    var inputFile = newInputFileMock(".github/workflows/deploy.yaml", "");

    assertThat(predicateWithLog.apply(inputFile)).isTrue();
    assertThat(logTester.logs()).contains("Identified as Github file: " + inputFile);
  }
}
