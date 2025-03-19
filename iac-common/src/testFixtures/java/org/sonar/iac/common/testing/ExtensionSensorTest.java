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
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ExtensionSensorTest extends AbstractSensorTest {

  protected static final String PARSING_ERROR_RULE_KEY = "S2260";

  protected abstract InputFile emptyFile();

  protected abstract InputFile fileWithParsingError();

  protected abstract Map<InputFile, Integer> validFilesMappedToExpectedLoCs();

  protected abstract InputFile validFile();

  protected abstract void verifyDebugMessages(List<String> logs);

  @Test
  void emptyFileShouldRaiseNoIssue() {
    analyze(sensor(checkFactory(PARSING_ERROR_RULE_KEY)), emptyFile());
    assertThat(context.allIssues()).isEmpty();
    assertThat(context.allAnalysisErrors()).isEmpty();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  protected void shouldRaiseIssueOnParsingErrorWhenIssueActive() {
    InputFile inputFile = fileWithParsingError();
    analyze(sensor(checkFactory(PARSING_ERROR_RULE_KEY)), inputFile);

    // Test issue
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo(PARSING_ERROR_RULE_KEY);
    assertThat(issue.ruleKey().repository()).as("A parsing error must be raised").isEqualTo(repositoryKey());
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("A parsing error occurred in this file.");
    assertThat(issue.primaryLocation().textRange()).isNotNull();

    // Test analysis warning
    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(analysisError.message()).startsWith("Unable to parse file:");

    // Test logging
    assertThat(logTester.logs(Level.WARN)).hasSize(1);
    assertThat(logTester.logs(Level.WARN).get(0))
      .startsWith("Cannot parse '%s:".formatted(inputFile));

    verifyDebugMessages(logTester.logs(Level.DEBUG));
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldRaiseNoIssueOnParsingErrorWhenIssueInactive() {
    analyze(sensor(checkFactory()), fileWithParsingError());
    assertThat(context.allIssues()).isEmpty();
    assertThat(context.allAnalysisErrors()).hasSize(1);
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void shouldRaiseNoIssueWhenSensorInactive() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyze(sensor(checkFactory(PARSING_ERROR_RULE_KEY)), fileWithParsingError());
    assertThat(context.allIssues()).isEmpty();
    assertThat(context.allAnalysisErrors()).isEmpty();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  protected void shouldRaiseIssueWhenFileCorrupted() throws IOException {
    InputFile inputFile = validFile();
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.contents()).thenThrow(IOException.class);
    analyze(spyInputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(spyInputFile);
    assertThat(analysisError.message()).startsWith("Unable to parse file:");
    assertThat(analysisError.location()).isNull();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  protected void shouldCorrectlyAddLinesOfCodesInAnalyzerTelemetryMetrics() {
    var totalLinesOfCode = validFilesMappedToExpectedLoCs().values().stream().mapToInt(Integer::intValue).sum();
    analyze(validFilesMappedToExpectedLoCs().keySet().toArray(new InputFile[0]));
    verifyLinesOfCodeTelemetry(totalLinesOfCode);
  }

}
