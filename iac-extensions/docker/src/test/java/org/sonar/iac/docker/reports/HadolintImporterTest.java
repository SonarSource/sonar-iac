/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.docker.reports;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonar.iac.docker.reports.hadolint.HadolintImporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;

class HadolintImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/hadolint";
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();
  private SensorContextTester context;

  @BeforeEach
  void setUp() {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);

    addFileToSensorContext(context, baseDir.toPath(), "docker-file.docker");
  }

  @ParameterizedTest
  @CsvSource({
    "/doesNotExist.json, Hadolint report importing: path does not seem to point to a file %s",
    "/parseError.json, Hadolint report importing: could not parse file as JSON %s",
    "/noArray.json, Hadolint report importing: file is expected to contain a JSON array but didn't %s"})
  void testProblemWhenReadingOrParsingFile(String reportPath, String expectedLog) {
    reportPath = PATH_PREFIX + reportPath;
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);

    importReport(reportFile);
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void testReadingIssue() {
    String path = "src\\test\\resources\\hadolint\\throwsIOException.json";
    File reportFile = Mockito.mock(File.class);
    String logMessage = String.format("Hadolint report importing: could not read report file %s", path);
    when(reportFile.getPath()).thenReturn(path);
    when(reportFile.isFile()).thenReturn(true);
    doAnswer((invocation) -> {
      throw new IOException();
    }).when(reportFile).toPath();

    importReport(reportFile);
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
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
    String logMessage = String.format("Hadolint report importing: could not save 1 out of 1 issues from %s.", reportFile.getPath());
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @ParameterizedTest
  @CsvSource({
    "/jsonFormat/validIssue.json",
    "/sonarqubeFormat/validIssue.json"})
  void testValidIssue(String reportPath) {
    File reportFile = new File(PATH_PREFIX + reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue).hasRuleId("DL3007");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo("Using latest is prone to errors if the image will ever update. Pin the version explicitly to a release tag");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(1);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void testValidIssueWithInvalidColumns() {
    File reportFile = new File(PATH_PREFIX + "/sonarqubeFormat/validIssueWithInvalidColumns.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue).hasRuleId("DL3007");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo("Using latest is prone to errors if the image will ever update. Pin the version explicitly to a release tag");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(1);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "/jsonFormat/validAndInvalid.json",
    "/sonarqubeFormat/validAndInvalid.json"})
  void testOneInvalidAndOneValidIssue(String reportPath) {
    File reportFile = new File(PATH_PREFIX + reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    String logMessage = String.format("Hadolint report importing: could not save 1 out of 2 issues from %s.", reportFile.getPath());
    assertThat(logTester.logs(Level.WARN))
      .containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @ParameterizedTest
  @CsvSource({
    "/jsonFormat/unknownRule.json",
    "/sonarqubeFormat/unknownRule.json"})
  void testUnknownRule(String reportPath) {
    File reportFile = new File(PATH_PREFIX + reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue).hasRuleId("hadolint.fallback");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "/jsonFormat/unknownWarningRuleWithValidHadolintName.json, SC9999",
    "/sonarqubeFormat/unknownWarningRuleWithValidHadolintName.json, DL9999"})
  void testUnknownWarningRuleWithValidHadolintFormat(String reportPath, String ruleId) {
    File reportFile = new File(PATH_PREFIX + reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue).hasRuleId(ruleId);
    assertThat(issue.severity()).isEqualTo(Severity.MAJOR);
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);

    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "/jsonFormat/unknownErrorRuleWithValidHadolintName.json, SC9999",
    "/sonarqubeFormat/unknownErrorRuleWithValidHadolintName.json, DL9999"})
  void testUnknownErrorRuleWithValidHadolintFormat(String reportPath, String ruleId) {
    File reportFile = new File(PATH_PREFIX + reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue).hasRuleId(ruleId);
    assertThat(issue.severity()).isEqualTo(Severity.CRITICAL);
    assertThat(issue.type()).isEqualTo(RuleType.BUG);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PATH_PREFIX + "/invalidPathTwo.json; Hadolint report importing: could not save 2 out of 2 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.docker, a/b/doesNotExistToo.docker",
    PATH_PREFIX + "/invalidPathMoreThanTwo.json; Hadolint report importing: could not save 3 out of 3 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.docker, a/b/doesNotExistToo.docker, ..."
  }, delimiter = ';')
  void testUnresolvedPathsAreAddedToWarning(File reportFile, String expectedLogFormat) {
    String expectedLog = String.format(expectedLogFormat, reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings, times(1)).addWarning(expectedLog);
  }

  private void importReport(File reportFile) {
    new HadolintImporter(context, mockAnalysisWarnings).importReport(reportFile);
  }
}
