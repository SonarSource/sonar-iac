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
package org.sonar.iac.cloudformation.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.cloudformation.plugin.CfnLintRulesDefinition;
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;

class CfnLintImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/cfn-lint";
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();
  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private final CfnLintRulesDefinition cfnLintRulesDefinition = new CfnLintRulesDefinition(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);

  @BeforeEach
  void setUp() {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);

    addFileToSensorContext(context, baseDir.toPath(), "template.yaml");
  }

  @ParameterizedTest
  @CsvSource({
    "/doesNotExist.json, Cfn-lint report importing: path does not seem to point to a file %s",
    "/parseError.json, Cfn-lint report importing: could not parse file as JSON %s",
    "/noArray.json, Cfn-lint report importing: file is expected to contain a JSON array but didn't %s"})
  void testProblemWhenReadingOrParsingFile(String reportPath, String expectedLog) {
    reportPath = PATH_PREFIX + reportPath;
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);

    importReport(reportFile);
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings).addWarning(logMessage);
  }

  @Test
  void testReadingIssue() {
    String path = "src\\test\\resources\\cfn-lint\\throwsIOException.json";
    File reportFile = Mockito.mock(File.class);
    String logMessage = String.format("Cfn-lint report importing: could not read report file %s", path);
    when(reportFile.getPath()).thenReturn(path);
    when(reportFile.isFile()).thenReturn(true);
    doAnswer(invocation -> {
      throw new IOException();
    }).when(reportFile).toPath();

    importReport(reportFile);
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings).addWarning(logMessage);
  }

  @Test
  void testNoIssues() {
    File reportFile = new File(PATH_PREFIX + "/emptyArray.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void testInvalidIssue() {
    File reportFile = new File(PATH_PREFIX + "/invalidIssue.json");
    String logMessage = String.format("Cfn-lint report importing: could not save 1 out of 1 issues from %s.", reportFile.getPath());
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings).addWarning(logMessage);
  }

  @Test
  void testValidIssue() {
    File reportFile = new File(PATH_PREFIX + "/validIssue.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    IacCommonAssertions.assertThat(issue).hasRuleId("E0000");
    assertThat(issue.type()).isEqualTo(RuleType.BUG);
    assertThat(issue.primaryLocation().message()).isEqualTo("Null value at line 8 column 20");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(8);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void testOneInvalidAndOneValidIssue() {
    File reportFile = new File(PATH_PREFIX + "/validAndInvalid.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    String logMessage = String.format("Cfn-lint report importing: could not save 1 out of 2 issues from %s.", reportFile.getPath());
    assertThat(logTester.logs(Level.WARN))
      .containsExactly(logMessage);
    verify(mockAnalysisWarnings).addWarning(logMessage);
  }

  @Test
  void testUnknownRule() {
    File reportFile = new File(PATH_PREFIX + "/unknownRule.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    IacCommonAssertions.assertThat(issue).hasRuleId("cfn-lint.fallback");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PATH_PREFIX + "/invalidPathTwo.json; Cfn-lint report importing: could not save 2 out of 2 issues from %s. Some file paths could not " +
      "be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml",
    PATH_PREFIX + "/invalidPathMoreThanTwo.json; Cfn-lint report importing: could not save 3 out of 3 issues from %s. Some file paths " +
      "could not be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml, ..."
  }, delimiter = ';')
  void unresolvedPathsShouldBeAddedToWarning(File reportFile, String expectedLogFormat) {
    String expectedLog = String.format(expectedLogFormat, reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings).addWarning(expectedLog);
  }

  private void importReport(File reportFile) {
    new CfnLintImporter(context, cfnLintRulesDefinition, mockAnalysisWarnings).importReport(reportFile);
  }
}
