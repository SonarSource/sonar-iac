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
package org.sonar.iac.terraform.reports.tflint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.TextRange;
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

class TFLintImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/tflint";
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.TRACE);

  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);

  @BeforeEach
  void setUp() throws IOException {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);

    addFileToContext(context, baseDir, PATH_PREFIX + "/exampleIssues.tf");
    addFileToContext(context, baseDir, PATH_PREFIX + "/exampleError.tf");
  }

  @Test
  void shouldImportExampleIssue() {
    File reportFile = new File(PATH_PREFIX + "/exampleIssues.json");
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("terraform_comment_syntax");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo("Single line comments should begin with #");
    assertTextRange(issue.primaryLocation().textRange(), 2, 0, 3, 0);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldImportExampleError() {
    File reportFile = new File(PATH_PREFIX + "/exampleError.json");
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("tflint.error");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo(
      "Failed to check ruleset; Failed to check `aws_instance_previous_type` rule: exampleError.tf:2,21-29: Reference to undeclared input variable; An input variable with the name \"type\" has not been declared. This variable can be declared with a variable \"type\" {} block.");
    assertTextRange(issue.primaryLocation().textRange(), 2, 0, 2, 26);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldImportExampleErrorBadFileLocation() {
    File reportFile = new File(PATH_PREFIX + "/exampleErrorBadFileLocation.json");
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("tflint.error");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo(
      "Failed to check ruleset; Failed to check `foo bar` rule: exampleError.tf:2,21-25: foo bar");
    assertTextRange(issue.primaryLocation().textRange(), 2, 20, 2, 24);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "/doesNotExist.json, TFLint report importing: path does not seem to point to a file %s",
    "/parseError.json, TFLint report importing: could not parse file as JSON %s",
    "/exampleErrorNoFilename.json, TFLint report importing: could not save 1 out of 1 issues from %s."})
  void shouldLogWarningWhenImport(String reportPath, String expectedLog) {
    reportPath = PATH_PREFIX + reportPath;
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(logTester.logs(Level.WARN)).containsExactly(String.format(expectedLog, path));
  }

  @Test
  void shouldLogWarningWhenReadingFileThrowsIOException() {
    String path = "src\\test\\resources\\tflint\\throwsIOException.json";
    File reportFile = Mockito.mock(File.class);
    String logMessage = String.format("TFLint report importing: could not read report file %s", path);
    when(reportFile.getPath()).thenReturn(path);
    when(reportFile.isFile()).thenReturn(true);
    doAnswer((invocation) -> {
      throw new IOException();
    }).when(reportFile).toPath();
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void shouldLogTraceWhenRuleDoesntExist() {
    String path = PATH_PREFIX + "/exampleIssueInvalidRuleId.json";
    File reportFile = new File(path);
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    String logMessage = "TFLint report importing:  No rule definition for rule id: id_doesnt_exist";
    assertThat(logTester.logs(Level.TRACE)).containsExactly(logMessage);
  }

  @ParameterizedTest
  @CsvSource(value = {
    PATH_PREFIX + "/invalidPathTwo.json; TFLint report importing: could not save 2 out of 2 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml",
    PATH_PREFIX + "/invalidPathMoreThanTwo.json; TFLint report importing: could not save 3 out of 3 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.yaml, a/b/doesNotExistToo.yaml, ..."
  }, delimiter = ';')
  void unresolvedPathsAreAddedToWarning(File reportFile, String expectedLogFormat) {
    String expectedLog = String.format(expectedLogFormat, reportFile.getPath());
    TFLintImporter importer = new TFLintImporter(context, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings, times(1)).addWarning(expectedLog);
  }

  private void assertTextRange(TextRange actual, int startLine, int startLineOffset, int endLine, int endLineOffset) {
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(actual.start().line()).as("startLine mismatch").isEqualTo(startLine);
      softly.assertThat(actual.start().lineOffset()).as("startLineOffset mismatch").isEqualTo(startLineOffset);
      softly.assertThat(actual.end().line()).as("endLine mismatch").isEqualTo(endLine);
      softly.assertThat(actual.end().lineOffset()).as("endLineOffset mismatch").isEqualTo(endLineOffset);
    });
  }
}
