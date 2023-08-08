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
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToContext;

class CfnLintImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/cfn-lint";
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private SensorContextTester context;

  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);

  @BeforeEach
  void setUp() throws IOException {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);

    addFileToContext(context, baseDir, PATH_PREFIX + "/template.yaml");
  }

  @ParameterizedTest
  @CsvSource({
    "/doesNotExist.json, Cfn-lint report importing: path does not seem to point to a file %s",
    "/parseError.json, Cfn-lint report importing: could not parse file as JSON %s",
    "/noArray.json, Cfn-lint report importing: file is expected to contain a JSON array but didn't %s"})
  void problem_when_reading_or_parsing_file(String reportPath, String expectedLog) {
    reportPath = PATH_PREFIX + reportPath;
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);

    importReport(reportFile);
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void reading_issue() {
    String path = "src\\test\\resources\\cfn-lint\\throwsIOException.json";
    File reportFile = Mockito.mock(File.class);
    String logMessage = String.format("Cfn-lint report importing: could not read report file %s", path);
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
  void no_issues() {
    File reportFile = new File(PATH_PREFIX + "/emptyArray.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void invalid_issue() {
    File reportFile = new File(PATH_PREFIX + "/invalidIssue.json");
    String logMessage = String.format("Cfn-lint report importing: could not save 1 out of 1 issues from %s.", reportFile.getPath());
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void valid_issue() {
    File reportFile = new File(PATH_PREFIX + "/validIssue.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("E0000");
    assertThat(issue.type()).isEqualTo(RuleType.BUG);
    assertThat(issue.primaryLocation().message()).isEqualTo("Null value at line 8 column 20");
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(8);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void one_invalid_and_one_valid_issue() {
    File reportFile = new File(PATH_PREFIX + "/validAndInvalid.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    String logMessage = String.format("Cfn-lint report importing: could not save 1 out of 2 issues from %s.", reportFile.getPath());
    assertThat(logTester.logs(Level.WARN))
      .containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void unknown_rule() {
    File reportFile = new File(PATH_PREFIX + "/unknownRule.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("cfn-lint.fallback");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PATH_PREFIX + "/invalidPathTwo.json; Cfn-lint report importing: could not save 2 out of 2 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml",
    PATH_PREFIX + "/invalidPathMoreThanTwo.json; Cfn-lint report importing: could not save 3 out of 3 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml, ..."
  }, delimiter = ';')
  void unresolvedPathsAreAddedToWarning(File reportFile, String expectedLogFormat) {
    String expectedLog = String.format(expectedLogFormat, reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings, times(1)).addWarning(expectedLog);
  }

  private void importReport(File reportFile) {
    new CfnLintImporter(context, mockAnalysisWarnings).importReport(reportFile);
  }
}
