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
package org.sonar.iac.common.reports;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;

class AbstractJsonReportImporterTest {

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private final AbstractExternalRulesDefinition mockRulesDefinition = mock(AbstractExternalRulesDefinition.class);
  private SensorContextTester context;

  @BeforeEach
  void setUp() {
    File baseDir = new File("src/test/resources/ext-json-report");
    context = SensorContextTester.create(baseDir);

    addFileToSensorContext(context, baseDir.toPath(), "noArray.json");
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
    TestImporter testImporter = new TestImporter(context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void shouldParseFile() {
    String filename = "src/test/resources/ext-json-report/validIssue.json";
    String path = File.separatorChar == '/' ? filename : Paths.get(filename).toString();
    File reportFile = new File(path);
    TestImporter testImporter = new TestImporter(context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(Level.INFO))
      .containsExactly(
        String.format("PREFIX Importing external report from: %s", path),
        "Issue saved");
  }

  @Test
  void shouldLogDebugWhenSaveIssueThrowsRuntimeException() {
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporterThrowReportImporterExceptionWhenSaveIssue testImporter = new TestImporterThrowReportImporterExceptionWhenSaveIssue(
      context,
      mockRulesDefinition,
      mockAnalysisWarnings,
      "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(Level.DEBUG)).containsExactly("failed to save issue");
  }

  @Test
  void shouldLogWarnUnresolvedPath() {
    String filePath = "src/test/resources/ext-json-report/validIssue.json";
    String path = File.separatorChar == '/' ? filePath : Paths.get(filePath).toString();
    File reportFile = new File(path);
    TestImporterThrowReportImporterExceptionWhenSaveIssue testImporter = new TestImporterThrowReportImporterExceptionWhenSaveIssue(
      context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");

    testImporter.importReport(reportFile);

    assertThat(logTester.logs(Level.WARN)).containsExactly(String.format(
      "PREFIX could not save 1 out of 1 issues from %s. " +
        "Some file paths could not be resolved: foo/bar",
      path));
  }

  @Test
  void asIntShouldSucceedOnLong() {
    long numberAsLong = 5;
    int numberAsInt = TestImporter.asInt(numberAsLong);
    assertThat(numberAsLong).isEqualTo(numberAsInt);
  }

  @Test
  void asIntShouldSucceedOnParsedJsonObject() throws ParseException {
    JSONParser jsonParser = new JSONParser();
    Object parsedJson = jsonParser.parse("5");
    int numberAsInt = TestImporter.asInt(parsedJson);
    assertThat(numberAsInt).isEqualTo(5);
  }

  @Test
  void asIntShouldFailOnAnythingOtherThanLongAndJsonWithLong() throws ParseException {
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
    TestImporter importer = new TestImporter(context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");

    Throwable throwable = catchThrowable(() -> importer.inputFile(null));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("Empty path");
  }

  @Test
  void shouldThrowExceptionWhenFileDoesntExist() {
    String path = "doNotExist.tf";
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporter importer = new TestImporter(context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");
    importer.importReport(reportFile);

    Throwable throwable = catchThrowable(() -> importer.inputFile(path));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("The file: doNotExist.tf could not be resolved");
  }

  @Test
  void shouldResolveFile() {
    String path = "noArray.json";
    File reportFile = new File("src/test/resources/ext-json-report/validIssue.json");
    TestImporter importer = new TestImporter(context, mockRulesDefinition, mockAnalysisWarnings, "PREFIX ");
    importer.importReport(reportFile);

    InputFile inputFile = importer.inputFile(path);

    assertThat(inputFile).isNotNull();
  }
}

class TestImporter extends AbstractJsonReportImporter {
  private static final Logger LOG = LoggerFactory.getLogger(TestImporter.class);

  protected TestImporter(SensorContext context, AbstractExternalRulesDefinition rulesDefinition, AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, rulesDefinition, analysisWarnings, warningPrefix);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    LOG.info("Issue saved");
    return mock(NewExternalIssue.class);
  }
}

class TestImporterThrowReportImporterExceptionWhenSaveIssue extends AbstractJsonReportImporter {

  protected TestImporterThrowReportImporterExceptionWhenSaveIssue(SensorContext context, AbstractExternalRulesDefinition rulesDefinition,
    AnalysisWarningsWrapper analysisWarnings, String warningPrefix) {
    super(context, rulesDefinition, analysisWarnings, warningPrefix);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    addUnresolvedPath("foo/bar");
    throw new ReportImporterException("saveIssue");
  }
}
