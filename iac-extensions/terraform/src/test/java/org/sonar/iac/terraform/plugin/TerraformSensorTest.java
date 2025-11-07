/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.terraform.plugin;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.MetricsVisitor;
import org.sonar.iac.common.extension.visitors.SyntaxHighlightingVisitor;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.testing.ExtensionSensorTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class TerraformSensorTest extends ExtensionSensorTest {

  @Test
  void shouldReturnTerraformDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor().describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("IaC Terraform Sensor");
    assertThat(descriptor.languages()).containsOnly("terraform");
  }

  @Test
  void shouldTestOneRule() {
    InputFile inputFile = inputFile("file1.tf", """
      resource "aws_s3_bucket" "myawsbucket" {
        tags = { "anycompany:cost-center" = "" }
      }""");
    analyze(sensor("S6273"), inputFile);
    Collection<Issue> issues = context.allIssues();
    assertThat(issues).hasSize(1);
    Issue issue = issues.iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo("S6273");
    IssueLocation location = issue.primaryLocation();
    assertThat(location.inputComponent()).isEqualTo(inputFile);
    assertThat(location.message())
      .isEqualTo("Rename tag key \"anycompany:cost-center\" to match the regular expression \"^(([^:]++:)*+([A-Z][A-Za-z]*+))$\".");
    org.sonar.api.batch.fs.TextRange issueTextRange = location.textRange();
    TextRange treeTextRange = TextRanges.range(issueTextRange.start().line(), issueTextRange.start().lineOffset(),
      issueTextRange.end().line(), issueTextRange.end().lineOffset());
    assertThat(treeTextRange).hasRange(2, 11, 2, 35);
    verifyLinesOfCodeTelemetry(3);
  }

  @Test
  void shouldRunInSonarLintContext() {
    InputFile inputFile = inputFile("file1.tf", """
      resource "aws_s3_bucket" "myawsbucket" {
        tags = { "anycompany:cost-center" = "" }
      }""");
    context.setRuntime(SONARLINT_RUNTIME_9_9);

    analyze(sensor("S6273"), inputFile);
    assertThat(context.allIssues()).hasSize(1);

    // No highlighting and metrics in SonarLint
    assertThat(context.highlightingTypeAt(inputFile.key(), 1, 0)).isEmpty();
    assertThat(context.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
    verifyLinesOfCodeTelemetry(0);
  }

  @Test
  void visitors() {
    assertThat(sensor().visitors(context, null)).hasSize(3);
  }

  @Test
  void shouldNotReturnHighlightingAndMetricsVisitorsInSonarLintContext() {
    List<TreeVisitor<InputFileContext>> visitors = sensor().visitors(sonarLintContext, null);
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
    return new TerraformSensor(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, fileLinesContextFactory, checkFactory, noSonarFilter,
      new TerraformLanguage(new MapSettings().asConfig()), providerVersions());
  }

  private TerraformProviders providerVersions() {
    return new TerraformProviders(new MapSettings().asConfig());
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
  protected Map<InputFile, Integer> validFilesMappedToExpectedLoCs() {
    return Map.of(
      validFile(), 1,
      inputFile("file2.tf", "a {}"), 1);
  }

  @Override
  protected void verifyDebugMessages(List<String> logs) {
    assertThat(logTester.logs(Level.DEBUG).get(0))
      .isEqualTo("""
        Parse error at line 1 column 4:

        1: a {
              ^
        """);

    assertThat(logTester.logs(Level.DEBUG).get(1))
      .startsWith("org.sonar.iac.common.extension.ParseException: Cannot parse 'parserError.tf:1:1'" +
        System.lineSeparator() +
        "\tat org.sonar.iac.common");

    assertThat(logTester.logs(Level.DEBUG)).hasSize(2);
  }
}
