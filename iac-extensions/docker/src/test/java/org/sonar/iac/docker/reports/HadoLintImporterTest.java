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
package org.sonar.iac.docker.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class HadoLintImporterTest {
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();
  private SensorContextTester context;

  @BeforeEach
  void setUp() throws IOException {
    File baseDir = new File("src/test/resources/hado-lint");
    context = SensorContextTester.create(baseDir);

    File someFile = new File("src/test/resources/hado-lint/docker-file.docker");
    context.fileSystem().add(new TestInputFileBuilder("project", baseDir, someFile).setContents(new String(Files.readAllBytes(someFile.toPath()))).build());
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/hado-lint/doesNotExist.json, Hado-lint report importing: path does not seem to point to a file %s",
    "src/test/resources/hado-lint/parseError.json, Hado-lint report importing: could not parse file as JSON %s",
    "src/test/resources/hado-lint/noArray.json, Hado-lint report importing: file is expected to contain a JSON array but didn't %s"})
  void problem_when_reading_or_parsing_file(String reportPath, String expectedLog) {
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);

    importReport(reportFile);
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void reading_issue() {
    String path = "src\\test\\resources\\hado-lint\\throwsIOException.json";
    File reportFile = Mockito.mock(File.class);
    String logMessage = String.format("Hado-lint report importing: could not read report file %s", path);
    when(reportFile.getPath()).thenReturn(path);
    when(reportFile.isFile()).thenReturn(true);
    doAnswer((invocation) -> {
      throw new IOException();
    }).when(reportFile).toPath();

    importReport(reportFile);
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void no_issues() {
    File reportFile = new File("src/test/resources/hado-lint/emptyArray.json");
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void invalid_issue() {
    File reportFile = new File("src/test/resources/hado-lint/invalidIssue.json");
    String logMessage = String.format("Hado-lint report importing: could not save 1 out of 1 issues from %s.", reportFile.getPath());
    importReport(reportFile);
    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/hado-lint/jsonFormat/validIssue.json",
    "src/test/resources/hado-lint/sonarqubeFormat/validIssue.json"})
  void valid_issue(String reportPath) {
    File reportFile = new File(reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.ruleId()).isEqualTo("DL3007");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.primaryLocation().message()).isEqualTo("Using latest is prone to errors if the image will ever update. Pin the version explicitly to a release tag");
    AssertionsForClassTypes.assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(10);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/hado-lint/jsonFormat/validAndInvalid.json",
    "src/test/resources/hado-lint/sonarqubeFormat/validAndInvalid.json"})
  void one_invalid_and_one_valid_issue(String reportPath) {
    File reportFile = new File(reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    String logMessage = String.format("Hado-lint report importing: could not save 1 out of 2 issues from %s.", reportFile.getPath());
    assertThat(logTester.logs(LoggerLevel.WARN))
      .containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/hado-lint/jsonFormat/unknownRule.json",
    "src/test/resources/hado-lint/sonarqubeFormat/unknownRule.json"})
  void unknown_rule(String reportPath) {
    File reportFile = new File(reportPath);
    importReport(reportFile);
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    AssertionsForClassTypes.assertThat(issue.ruleId()).isEqualTo("hado-lint.fallback");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @ParameterizedTest
  @CsvSource(value = {
    "src/test/resources/hado-lint/invalidPathTwo.json; Hado-lint report importing: could not save 2 out of 2 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.docker, a/b/doesNotExistToo.docker",
    "src/test/resources/hado-lint/invalidPathMoreThanTwo.json; Hado-lint report importing: could not save 3 out of 3 issues from %s. Some file paths could not be resolved: " +
      "doesNotExist.docker, a/b/doesNotExistToo.docker, ..."
  }, delimiter = ';')
  void unresolvedPathsAreAddedToWarning(File reportFile, String expectedLogFormat) {
    String expectedLog = String.format(expectedLogFormat, reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings, times(1)).addWarning(expectedLog);
  }

  private void importReport(File reportFile) {
    new HadoLintImporter(context, mockAnalysisWarnings).importReport(reportFile);
  }
}
