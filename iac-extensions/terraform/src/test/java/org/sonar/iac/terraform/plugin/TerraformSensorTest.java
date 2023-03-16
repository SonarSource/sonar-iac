/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.notifications.AnalysisWarnings;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.DurationStatistics;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TerraformSensorTest extends ExtensionSensorTest {

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
    org.sonar.api.batch.fs.TextRange issueTextRange = location.textRange();
    TextRange treeTextRange = TextRanges.range(issueTextRange.start().line(), issueTextRange.start().lineOffset(),
      issueTextRange.end().line(), issueTextRange.end().lineOffset());
    TextRangeAssert.assertTextRange(treeTextRange).hasRange(2, 11, 2, 35);
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
  void visitors() {
    assertThat(sensor().visitors(context, mock(DurationStatistics.class))).hasSize(3);
  }

  @Test
  void shouldNotReturnHighlightingAndMetricsVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, mock(DurationStatistics.class));
    assertThat(visitors).doesNotHaveAnyElementsOfTypes(SyntaxHighlightingVisitor.class, MetricsVisitor.class);
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
    return new TerraformSensor(SONAR_RUNTIME_8_9, fileLinesContextFactory, checkFactory, noSonarFilter, new TerraformLanguage(new MapSettings().asConfig()), providerVersions());
  }

  private TerraformProviders providerVersions() {
    return new TerraformProviders(context, mock(AnalysisWarnings.class));
  }

  @Override
  protected String repositoryKey() {
    return TerraformExtension.REPOSITORY_KEY;
  }

  @Override
  protected String fileLanguageKey() {
    return TerraformLanguage.KEY;
  }

  @Override
  protected InputFile emptyFile() {
    return inputFile("empty.tf", "");
  }

  @Override
  protected InputFile fileWithParsingError() {
    return inputFile("parserError.tf", "a {");
  }

  @Override
  protected InputFile validFile() {
    return inputFile("file.tf", "a {}");
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(LoggerLevel.DEBUG).get(0))
      .isEqualTo("Parse error at line 1 column 4:\n" +
      "\n" +
      "1: a {\n" +
      "      ^\n");

    assertThat(logTester.logs(LoggerLevel.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'parserError.tf:1:1'" +
        System.lineSeparator() +
        "\tat org.sonar.iac.common");

    assertThat(logTester.logs(LoggerLevel.DEBUG)).hasSize(2);
  }
}
