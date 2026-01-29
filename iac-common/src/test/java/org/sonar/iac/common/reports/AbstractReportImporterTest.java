/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.common.reports;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.slf4j.event.Level;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;

/**
 * Tests for {@link AbstractReportImporter} base class functionality.
 * These tests cover methods that are only defined in the base class and not tested by subclass tests.
 */
class AbstractReportImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/abstract-report";

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private final AbstractExternalRulesDefinition mockRulesDefinition = mock(AbstractExternalRulesDefinition.class);

  @BeforeEach
  void setUp() {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);
    addFileToSensorContext(context, baseDir.toPath(), "test-file.txt");
  }

  // ==================== parseJson() - IOException handling (consolidated from subclass tests) ====================

  @Test
  void parseJson_shouldLogWarningForIOException() {
    String path = "test-io-exception.json";
    File reportFile = Mockito.mock(File.class);
    when(reportFile.getPath()).thenReturn(path);
    when(reportFile.isFile()).thenReturn(true);
    doAnswer(invocation -> {
      throw new IOException("Simulated IO error");
    }).when(reportFile).toPath();

    createImporter().importReport(reportFile);

    String expectedLog = "Test: could not read report file " + path;
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings).addWarning(expectedLog);
  }

  // ==================== addWarning() - tests truncation of >2 unresolved paths ====================

  @Test
  void addWarning_shouldLimitUnresolvedPathsToTwo() {
    File reportFile = new File(PATH_PREFIX + "/threeIssues.json");
    BaseTestImporterWithMultipleUnresolvedPaths importer = new BaseTestImporterWithMultipleUnresolvedPaths(
      context, mockRulesDefinition, mockAnalysisWarnings);

    importer.importReport(reportFile);

    assertThat(logTester.logs(Level.WARN))
      .anyMatch(log -> log.contains("path1.txt, path2.txt, ..."));
  }

  // ==================== asInt() tests (moved from JsonArray test - tests base class utility method) ====================

  @Test
  void asInt_shouldSucceedOnLong() {
    long numberAsLong = 5;
    int numberAsInt = BaseTestImporter.asIntPublic(numberAsLong);
    assertThat(numberAsLong).isEqualTo(numberAsInt);
  }

  @Test
  void asInt_shouldSucceedOnParsedJsonLong() throws ParseException {
    JSONParser jsonParser = new JSONParser();
    Object parsedJson = jsonParser.parse("5");
    int numberAsInt = BaseTestImporter.asIntPublic(parsedJson);
    assertThat(numberAsInt).isEqualTo(5);
  }

  @Test
  void asInt_shouldFailOnInvalidTypes() throws ParseException {
    SoftAssertions softly = new SoftAssertions();

    JSONParser jsonParser = new JSONParser();
    Object parsedDouble = jsonParser.parse("1.5");

    List<Object> objects = List.of(5, (short) 5, "string", parsedDouble);

    for (Object object : objects) {
      softly.assertThatExceptionOfType(ClassCastException.class)
        .isThrownBy(() -> BaseTestImporter.asIntPublic(object));
    }

    softly.assertAll();
  }

  // ==================== inputFile() tests (moved from JsonArray test - tests base class method) ====================

  @Test
  void inputFile_shouldThrowExceptionWhenFilenameIsNull() {
    BaseTestImporter importer = createImporter();
    // Need to call importReport first to initialize unresolvedPaths
    importer.importReport(new File(PATH_PREFIX + "/singleIssue.json"));

    Throwable throwable = catchThrowable(() -> importer.inputFile(null));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("Empty path");
  }

  @Test
  void inputFile_shouldThrowExceptionWhenFileDoesNotExist() {
    String path = "nonexistent.txt";
    BaseTestImporter importer = createImporter();
    importer.importReport(new File(PATH_PREFIX + "/singleIssue.json"));

    Throwable throwable = catchThrowable(() -> importer.inputFile(path));

    assertThat(throwable)
      .isInstanceOf(ReportImporterException.class)
      .hasMessage("The file: nonexistent.txt could not be resolved");
  }

  @Test
  void inputFile_shouldResolveExistingFile() {
    String path = "test-file.txt";
    BaseTestImporter importer = createImporter();
    importer.importReport(new File(PATH_PREFIX + "/singleIssue.json"));

    InputFile inputFile = importer.inputFile(path);

    assertThat(inputFile).isNotNull();
  }

  // ==================== Helper methods ====================

  private BaseTestImporter createImporter() {
    return new BaseTestImporter(context, mockRulesDefinition, mockAnalysisWarnings);
  }
}

/**
 * Minimal test implementation of AbstractReportImporter for testing base class behavior.
 */
class BaseTestImporter extends AbstractReportImporter {

  protected BaseTestImporter(SensorContext context, AbstractExternalRulesDefinition rulesDefinition,
    AnalysisWarningsWrapper analysisWarnings) {
    super(context, rulesDefinition, analysisWarnings, "Test: ");
  }

  @Override
  protected JSONArray extractIssues(File reportFile) throws IOException, ParseException {
    try (var reader = Files.newBufferedReader(reportFile.toPath())) {
      return (JSONArray) jsonParser.parse(reader);
    }
  }

  @Override
  protected String getExpectedFileFormat() {
    return "JSON array";
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    return mock(NewExternalIssue.class);
  }

  // Expose protected method for testing
  public static int asIntPublic(Object o) {
    return asInt(o);
  }
}

/**
 * Test importer that adds multiple unresolved paths, for testing path limit in warnings.
 */
class BaseTestImporterWithMultipleUnresolvedPaths extends BaseTestImporter {
  private int callCount = 0;

  protected BaseTestImporterWithMultipleUnresolvedPaths(SensorContext context, AbstractExternalRulesDefinition rulesDefinition,
    AnalysisWarningsWrapper analysisWarnings) {
    super(context, rulesDefinition, analysisWarnings);
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    callCount++;
    addUnresolvedPath("path" + callCount + ".txt");
    throw new ReportImporterException("Test exception");
  }
}
