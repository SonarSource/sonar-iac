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
package org.sonar.iac.common.reports;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AbstractJsonReportImporterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private SensorContextTester context;

  @BeforeEach
  void setUp() {
    File baseDir = new File("src/test/resources/ext-json-report");
    context = SensorContextTester.create(baseDir);
  }

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/ext-json-report/doesNotExist.json, PREFIX path does not seem to point to a file %s",
    "src/test/resources/ext-json-report/parseError.json, PREFIX could not parse file as JSON %s",
    "src/test/resources/ext-json-report/noArray.json, PREFIX file is expected to contain a JSON array but didn't %s"})
  void shouldLogWarnForFile(String reportPath, String expectedLog) {
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);
    TestImporter testImporter = new TestImporter(context, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void shouldParseFile() {
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporter testImporter = new TestImporter(context, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(LoggerLevel.INFO)).containsExactly("Issue saved");
  }

  @Test
  void shouldLogDebugWhenSaveIssueThrowsRuntimeException() {
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporterThrowRuntimeWhenSaveIssue testImporter = new TestImporterThrowRuntimeWhenSaveIssue(
      context,
      mockAnalysisWarnings,
      "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(LoggerLevel.DEBUG)).containsExactly("failed to save issue");
  }

  @Test
  void shouldLogWarnUnresolvedPath() {
    String filePath = "src/test/resources/ext-json-report/validIssue.json";
    String path = File.separatorChar == '/' ? filePath : Paths.get(filePath).toString();
    File reportFile = new File(path);
    TestImporterThrowRuntimeWhenSaveIssue testImporter = new TestImporterThrowRuntimeWhenSaveIssue(
      context, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(String.format(
      "PREFIX could not save 1 out of 1 issues from %s. " +
        "Some file paths could not be resolved: foo/bar", path));
  }
}

class TestImporter extends AbstractJsonReportImporter {
  private static final Logger LOG = Loggers.get(TestImporter.class);

  protected TestImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, analysisWarnings, warningPrefix);
  }

  @Override
  protected void saveIssue(JSONObject issueJson) {
    LOG.info("Issue saved");
  }
}

class TestImporterThrowRuntimeWhenSaveIssue extends AbstractJsonReportImporter {

  protected TestImporterThrowRuntimeWhenSaveIssue(SensorContext context, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, analysisWarnings, warningPrefix);
  }

  @Override
  protected void saveIssue(JSONObject issueJson) {
    addUnresolvedPath("foo/bar");
    throw new RuntimeException("saveIssue");
  }
}

class TestImporterAddUnresolvedPathWhenSave extends AbstractJsonReportImporter {

  protected TestImporterAddUnresolvedPathWhenSave(SensorContext context, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, analysisWarnings, warningPrefix);
  }

  @Override
  protected void saveIssue(JSONObject issueJson) {
    addUnresolvedPath("foo/bar");
  }
}
