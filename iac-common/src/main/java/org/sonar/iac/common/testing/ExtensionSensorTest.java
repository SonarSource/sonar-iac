/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.common.testing;

import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.utils.log.LoggerLevel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public abstract class ExtensionSensorTest extends AbstractSensorTest {

  protected abstract InputFile emptyFile();

  protected abstract InputFile fileWithParsingError();

  protected abstract InputFile validFile();

  @Test
  void emptyFileShouldRaiseNoIssue() {
    analyse(sensor(checkFactory("S2260")), emptyFile());
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
    assertThat(context.allAnalysisErrors()).isEmpty();
  }

  @Test
  void shouldRaiseIssueOnParsingErrorWhenIssueActive() {
    InputFile inputFile = fileWithParsingError();
    analyse(sensor(checkFactory("S2260")), inputFile);

    // Test issue
    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("A parsing error occurred in this file.");
    TextRange range = issue.primaryLocation().textRange();
    assertThat(range).isNotNull();

    // Test analysis warning
    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(inputFile);
    assertThat(analysisError.message()).startsWith("Unable to parse file:");

    // Test logging
    assertThat(logTester.logs(LoggerLevel.ERROR)).hasSize(2);
    assertThat(logTester.logs(LoggerLevel.ERROR)).extracting(s -> s)
      .anyMatch(s -> s.startsWith(String.format("Unable to parse file: %s.", inputFile.uri())))
      .anyMatch(s -> s.startsWith(String.format("Cannot parse '%s':", inputFile.filename())));
  }

  @Test
  void shouldRaiseNoIssueOnParsingErrorWhenIssueInactive() {
    analyse(sensor(checkFactory()), fileWithParsingError());
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  void shouldRaiseNoIssueWhenSensorInactive() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyse(sensor(checkFactory("S2260")), fileWithParsingError());
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
    assertThat(context.allAnalysisErrors()).isEmpty();
  }

  @Test
  void shouldRaiseIssueWhenFileCorrupted() throws IOException {
    InputFile inputFile = validFile();
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.contents()).thenThrow(IOException.class);
    analyse(spyInputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(spyInputFile);
    assertThat(analysisError.message()).startsWith("Unable to parse file:");
    assertThat(analysisError.location()).isNull();
  }
}
