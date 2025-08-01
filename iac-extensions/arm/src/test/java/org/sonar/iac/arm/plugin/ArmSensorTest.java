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
package org.sonar.iac.arm.plugin;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.iac.arm.visitors.ArmHighlightingVisitor;
import org.sonar.iac.arm.visitors.ArmSymbolVisitor;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.ChecksVisitor;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.IacTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.sonar.iac.arm.plugin.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.arm.plugin.ArmJsonFilePredicate.ARM_JSON_FILE_IDENTIFIER_KEY;
import static org.sonar.iac.common.extension.IacSensor.EXTENDED_LOGGING_PROPERTY_NAME;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class ArmSensorTest extends ExtensionSensorTest {

  @Test
  void shouldReturnArmDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Azure Resource Manager Sensor");
    assertThat(descriptor.languages()).containsExactly("json", "azureresourcemanager");
    assertThat(descriptor.isProcessesFilesIndependently()).isTrue();
  }

  /**
   * When trying to identify if an input file is an ARM file, we look for a specific identifier in the file.
   * There is a limit (8 Kb) on the file reading to not spend an extensive amount of time.
   * This test checks 2 files with valid identifiers. The identifiers are at the end of each file.
   */
  @Test
  void shouldFastCheckFilePredicate() {
    InputFile largeFileWithIdentifier = IacTestUtils.inputFile("large_file_with_identifier.json", "json");
    InputFile mediumFileWithIdentifier = IacTestUtils.inputFile("medium_file_with_identifier.json", "json");

    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);

    FilePredicate filePredicate = sensor().customFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(largeFileWithIdentifier)).isFalse();
    assertThat(filePredicate.apply(mediumFileWithIdentifier)).isTrue();
  }

  @Test
  void shouldAllowBicepFiles() {
    InputFile mainBicepInputFile = TestInputFileBuilder.create("moduleKey", "main")
      .setLanguage("azureresourcemanager")
      .setType(InputFile.Type.MAIN)
      .build();

    InputFile testBicepInputFile = TestInputFileBuilder.create("moduleKey", "test")
      .setLanguage("azureresourcemanager")
      .setType(InputFile.Type.TEST)
      .build();

    FilePredicate filePredicate = sensor().mainFilePredicate(context, new DurationStatistics(mock(Configuration.class)));
    assertThat(filePredicate.apply(mainBicepInputFile)).isTrue();
    assertThat(filePredicate.apply(testBicepInputFile)).isFalse();
  }

  @Override
  protected String getActivationSettingKey() {
    return ArmSettings.ACTIVATION_KEY;
  }

  @Override
  protected ArmSensor sensor(CheckFactory checkFactory) {
    return new ArmSensor(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, fileLinesContextFactory, checkFactory, noSonarFilter,
      new ArmLanguage(new MapSettings().asConfig()));
  }

  private ArmSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String repositoryKey() {
    return ArmExtension.REPOSITORY_KEY;
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
    return inputFile("error.json", "\"a'");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("object.json", "{\"key\":\"value\"}");
  }

  @Override
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      // json
      validFile(), 1,
      // bicep
      IacTestUtils.inputFile(
        "object.bicep",
        baseDir.toPath(),
        "param environmentName string",
        "azureresourcemanager"),
      1);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
    String message1 = """
      while scanning a quoted scalar
       in reader, line 1, column 1:
          "a'
          ^
      found unexpected end of stream
       in reader, line 1, column 4:
          "a'
             ^
      """;
    String message2 = "org.sonar.iac.common.extension.ParseException: Cannot parse 'error.json:1:1'" +
      System.lineSeparator() +
      "\tat org.sonar.iac.common";
    assertThat(logTester.logs(Level.DEBUG).get(0)).isEqualTo(message1);
    assertThat(logTester.logs(Level.DEBUG).get(1)).startsWith(message2);
    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }

  @Test
  void shouldIncludeAllVisitorsInRegularContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(context, null);
    assertThat(visitors)
      .hasOnlyElementsOfTypes(ArmSymbolVisitor.class, MetricsVisitor.class, ChecksVisitor.class, ArmHighlightingVisitor.class)
      .hasSize(4);
  }

  @Test
  void shouldNotIncludeSomeVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, null);
    assertThat(visitors)
      .doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class)
      .hasExactlyElementsOfTypes(ArmSymbolVisitor.class, ChecksVisitor.class);
  }

  @Test
  void shouldLogPredicateInDurationStatistics() {
    settings.setProperty("sonar.iac.duration.statistics", "true");

    InputFile bicepFile = inputFile("valid.bicep", "");
    InputFile armJsonFile = inputFile("valid.json", "{\"$schema\": \"https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#\"}\n");

    analyze(sensor(), bicepFile, armJsonFile);
    assertThat(durationStatisticLog()).contains("ArmJsonFilePredicate");
  }

  @Test
  void shouldRaiseNoParsingIssueInFileWithoutCorrectIdentifierAndLogPredicateFailure() {
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    analyze(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    var logs = logTester.logs(Level.DEBUG);
    assertThat(logs).hasSize(1);
    assertThat(logs.get(0))
      .startsWith("File without any identifiers").endsWith("parserError.json");
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldNotLogWhenExtendedLoggingIsDisabledForFileWithoutCorrectIdentifier() {
    settings.setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
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
    context.settings().setProperty(ARM_JSON_FILE_IDENTIFIER_KEY, ARM_JSON_FILE_IDENTIFIER_DEFAULT_VALUE);
    analyze(sensor("S2260"), inputFile("parserError.json", "\"noIdentifier'"));
    assertThat(context.allIssues()).isEmpty();

    assertThat(logTester.logs(Level.DEBUG)).isEmpty();
    verifyLinesOfCodeTelemetry(0);
  }
}
