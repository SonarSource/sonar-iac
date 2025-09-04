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
package org.sonar.iac.cloudformation.plugin;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.extension.IacSensor.EXTENDED_LOGGING_PROPERTY_NAME;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class CloudformationSensorTest extends ExtensionSensorTest {

  private static final String PARSING_ERROR_KEY = "S2260";

  @Test
  void yamlFileWithRecursiveAnchorReferenceShouldRaiseParsingIssue() {
    analyze(sensor(checkFactory(PARSING_ERROR_KEY)), inputFile("loop.yaml",
      """
        AWSTemplateFormatVersion: 2010-09-09
        foo: &fooanchor
          bar: *fooanchor"""));

    assertThat(context.allAnalysisErrors()).hasSize(1);
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_KEY);
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldReturnCloudformationDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC CloudFormation Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "yaml", "cloudformation");
  }

  @Test
  void shouldRaiseNoParsingIssueInFileWithoutCorrectIdentifier() {
    context.settings().setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "myIdentifier");

    analyze(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
      .startsWith("File without identifier 'myIdentifier':").endsWith("parserError.json");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsDisabledForFileWithoutCorrectIdentifier() {
    settings.setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "myIdentifier");
    settings.setProperty(EXTENDED_LOGGING_PROPERTY_NAME, false);
    analyze(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsOnDefaultForFileWithoutCorrectIdentifier() {
    context.setSettings(new MapSettings());
    context.settings().setProperty(getActivationSettingKey(), true);
    context.settings().setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "myIdentifier");
    analyze(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldRaiseParsingIssueInFileWithIdentifier() {
    context.settings().setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, "myIdentifier");

    analyze(sensor("S2260"), inputFile("parserError.json", "\"myIdentifier'"));
    assertThat(context.allIssues()).hasSize(1);
    verifyLinesOfCodeTelemetry(0);
  }

  /**
   * When identifying whether an input file is a Cloudformation file, a custom identifier is retrieved from the file.
   * In order not to spend too much time on this verification in large files, it is only applied to the first 8kb.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.json", "json");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.json", "json");

    context.settings().setProperty(CLOUDFORMATION_FILE_IDENTIFIER_KEY, CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE);

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  @Test
  void shouldSkipCloudFormationFileInGithubWorkflowFolder() {
    var githubWorkflowFile = inputFile(".github/workflows/deploy.yaml", "AWSTemplateFormatVersion: 2010-09-09");

    analyze(sensor(checkFactory()), githubWorkflowFile);
    assertThat(context.allIssues()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0)).contains("Identified as Github file: .github/workflows/deploy.yaml");
    assertThat(logTester.logs(Level.INFO)).contains("0 source files to be analyzed", "0/0 source files have been analyzed");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldLogPredicateInDurationStatistics() {
    settings.setProperty("sonar.iac.duration.statistics", "true");

    InputFile jvmFile = inputFile("file.json", "");

    analyze(sensor(checkFactory()), jvmFile);
    assertThat(durationStatisticLog()).contains("CloudFormationFilePredicate", "CloudFormationNotGithubActionsFilePredicate");
    verifyLinesOfCodeTelemetry(0);
  }

  private CloudformationSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String getActivationSettingKey() {
    return CloudformationSettings.ACTIVATION_KEY;
  }

  @Override
  protected CloudformationSensor sensor(CheckFactory checkFactory) {
    return new CloudformationSensor(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, fileLinesContextFactory, checkFactory, noSonarFilter,
      new CloudformationLanguage());
  }

  @Override
  protected String repositoryKey() {
    return CloudformationExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return "json";
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("empty.json", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("error.json", "AWSTemplateFormatVersion: \"a'");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("comment.yaml", "AWSTemplateFormatVersion: 2010-09-09\n# Some Comment");
  }

  @Override
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      validFile(), 1,
      inputFile("comment2.yaml", "AWSTemplateFormatVersion: 2010-09-09\n# Some Comment"), 1);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
    String message1 = """
      while scanning a quoted scalar
       in reader, line 1, column 27:
          AWSTemplateFormatVersion: "a'
                                    ^
      found unexpected end of stream
       in reader, line 1, column 30:
          AWSTemplateFormatVersion: "a'
                                       ^
      """;
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'error.json:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1)).startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
