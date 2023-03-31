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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
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
  void setUp() throws IOException {
    File baseDir = new File("src/test/resources/ext-json-report");
    context = SensorContextTester.create(baseDir);

    addFileToContext(baseDir, "src/test/resources/ext-json-report/noArray.json");
  }

  private void addFileToContext(File baseDir, String path) throws IOException {
    File someFile = new File(path);
    context.fileSystem().add(new TestInputFileBuilder("project", baseDir, someFile).setContents(new String(Files.readAllBytes(someFile.toPath()))).build());
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

    assertThat(logTester.logs(LoggerLevel.INFO))
      .containsExactly(
        "PREFIX  Importing external report from: src/test/resources/ext-json-report/validIssue.json",
        "Issue saved");
  }

  @Test
  void shouldLogDebugWhenSaveIssueThrowsRuntimeException() {
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporterThrowReportImporterExceptionWhenSaveIssue testImporter = new TestImporterThrowReportImporterExceptionWhenSaveIssue(
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
    TestImporterThrowReportImporterExceptionWhenSaveIssue testImporter = new TestImporterThrowReportImporterExceptionWhenSaveIssue(
      context, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(LoggerLevel.WARN)).containsExactly(String.format(
      "PREFIX could not save 1 out of 1 issues from %s. " +
        "Some file paths could not be resolved: foo/bar",
      path));
  }

  @Test
  void asIntSucceedsOnLong() {
    long numberAsLong = 5;
    int numberAsInt = TestImporter.asInt(numberAsLong);
    assertThat(numberAsLong).isEqualTo(numberAsInt);
  }

  @Test
  void asIntSucceedsOnParsedJsonObject() throws ParseException {
    JSONParser jsonParser = new JSONParser();
    Object parsedJson = jsonParser.parse("5");
    int numberAsInt = TestImporter.asInt(parsedJson);
    assertThat(numberAsInt).isEqualTo(5);
  }

  @Test
  void asIntFailsOnAnythingOtherThanLongAndJsonWithLong() throws ParseException {
    SoftAssertions softly = new SoftAssertions();

    JSONParser jsonParser = new JSONParser();
    Object parsedDouble = jsonParser.parse("1.5");

    List<Object> objects = List.of(5, (short) 5, "string", parsedDouble);

    for (Object object : objects) {
      softly.assertThatExceptionOfType(ClassCastException.class)
        .isThrownBy(() -> TestImporter.asInt(object));
    }

    softly.assertAll();
  }

  @Test
  void shouldThrowExceptionWhenNullReportFile() {
    TestImporter importer = new TestImporter(context, mockAnalysisWarnings, "PREFIX ");

    Throwable throwable = catchThrowable(() -> importer.inputFile(null));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("Empty path");
  }

  @Test
  void shouldThrowExceptionWhenFileDoesntExist() {
    String path = "doNotExist.tf";
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporter importer = new TestImporter(context, mockAnalysisWarnings, "PREFIX ");
    importer.importReport(reportFile);

    Throwable throwable = catchThrowable(() -> importer.inputFile(path));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("The file: doNotExist.tf is not resolved");
  }

  @Test
  void shouldResolveFile() {
    String path = "noArray.json";
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporter importer = new TestImporter(context, mockAnalysisWarnings, "PREFIX ");
    importer.importReport(reportFile);

    InputFile inputFile = importer.inputFile(path);

    assertThat(inputFile).isNotNull();
  }
}

class TestImporter extends AbstractJsonReportImporter {
  private static final Logger LOG = Loggers.get(TestImporter.class);

  protected TestImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, analysisWarnings, warningPrefix);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    LOG.info("Issue saved");
    return mock(NewExternalIssue.class);
  }
}

class TestImporterThrowReportImporterExceptionWhenSaveIssue extends AbstractJsonReportImporter {

  protected TestImporterThrowReportImporterExceptionWhenSaveIssue(SensorContext context, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, analysisWarnings, warningPrefix);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    addUnresolvedPath("foo/bar");
    throw new ReportImporterException("saveIssue");
  }
}
