/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.error.AnalysisError;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.testing.AbstractSensorTest;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class TerraformSensorTest extends AbstractSensorTest {

  @Test
  void should_return_terraform_descriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Terraform Sensor");
    assertThat(descriptor.languages()).containsOnly("terraform");
  }

  @Test
  void test_one_rule() {
    InputFile inputFile = inputFile("file1.tf", "" +
      "resource \"aws_s3_bucket\" \"myawsbucket\" {\n" +
      "  tags = { \"anycompany:cost-center\" = \"\" }\n" +
      "}");
    analyse(sensor("S6273"), inputFile);
    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("S6273");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message()).isEqualTo("Rename tag key \"anycompany:cost-center\" to match the regular expression \"^([A-Z][A-Za-z]*:)*([A-Z][A-Za-z]*)$\".");
    TextRangeAssert.assertTextRange(location.textRange()).hasRange(2, 11, 2, 35);
  }


  @Test
  void empty_file_should_raise_no_issue() {
    analyse(sensor("S2260"), inputFile("empty.tf", ""));
    assertThat(context.allIssues()).as("No issue must be raised").isEmpty();
  }

  @Test
  void parsing_error_should_raise_an_issue_if_check_rule_is_activated() {
    analyse(sensor("S2260"), inputFile("parserError.tf", "a {"));

    assertThat(context.allIssues()).as("One issue must be raised").hasSize(1);

    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).as("A parsing error must be raised").isEqualTo("S2260");

    TextRange range = issue.primaryLocation().textRange();
    assertThat(range).isNotNull();
    assertThat(range.start().line()).isEqualTo(1);
    assertThat(range.start().lineOffset()).isEqualTo(0);
    assertThat(range.end().line()).isEqualTo(1);
    assertThat(range.end().lineOffset()).isEqualTo(3);
  }

  @Test
  void parsing_error_should_raise_issue_in_sensor_context() {
    analyse(inputFile("parserError.tf", "a {"));
    assertThat(context.allAnalysisErrors()).hasSize(1);
  }

  @Test
  void parsing_error_should_raise_no_issue_if_check_rule_is_not_activated() {
    analyse(inputFile("parserError.tf", "a {"));
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  @Test
  void analysis_error_should_raise_issue_in_sensor_context() throws IOException {
    InputFile inputFile = inputFile("fakeFile.tf", "");
    InputFile spyInputFile = spy(inputFile);
    when(spyInputFile.contents()).thenThrow(IOException.class);
    analyse(spyInputFile);

    Collection<AnalysisError> analysisErrors = context.allAnalysisErrors();
    assertThat(analysisErrors).hasSize(1);
    AnalysisError analysisError = analysisErrors.iterator().next();
    assertThat(analysisError.inputFile()).isEqualTo(spyInputFile);
    assertThat(analysisError.message()).isEqualTo("Unable to parse file: fakeFile.tf");
    assertThat(analysisError.location()).isNull();

    assertThat(logTester.logs()).contains(String.format("Unable to parse file: %s. ", inputFile.uri()));
  }


  @Test
  void no_parsing_nor_analysis_error_on_valid_file() {
    analyse(inputFile("file.tf", "a {}"));
    assertThat(context.allAnalysisErrors()).isEmpty();
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  void test_sonarlint_context() {
    SonarRuntime sonarLintRuntime = SonarRuntimeImpl.forSonarLint(Version.create(6, 0));
    InputFile inputFile = inputFile("file1.tf", "" +
      "resource \"aws_s3_bucket\" \"myawsbucket\" {\n" +
      "  tags = { \"anycompany:cost-center\" = \"\" }\n" +
      "}");
    context.setRuntime(sonarLintRuntime);

    analyse(sensor("S6273"), inputFile);
    assertThat(context.allIssues()).hasSize(1);

    // No highlighting and metrics in SonarLint
    assertThat(context.highlightingTypeAt(inputFile.key(), 1, 0)).isEmpty();
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
  }

  @Test
  void should_raise_no_issue_when_sensor_deactivated() {
    MapSettings settings = new MapSettings();
    settings.setProperty(getActivationSettingKey(), false);
    context.setSettings(settings);

    analyse(sensor("S2260"), inputFile("parserError.tf", "a {"));
    assertThat(context.allIssues()).as("One issue must be raised").isEmpty();
  }

  private TerraformSensor sensor(String... rules) {
    return sensor(checkFactory(rules));
  }

  @Override
  protected String getActivationSettingKey() {
    return TerraformSettings.ACTIVATION_KEY;
  }

  @Override
  protected TerraformSensor sensor(CheckFactory checkFactory) {
    return new TerraformSensor(fileLinesContextFactory, checkFactory, noSonarFilter, language());
  }

  @Override
  protected String repositoryKey() {
    return TerraformExtension.REPOSITORY_KEY;
  }

  @Override
  protected TerraformLanguage language() {
    return new TerraformLanguage(new MapSettings().asConfig());
  }

  @Override
  protected String fileLanguageKey() {
    return language().getKey();
  }
}
