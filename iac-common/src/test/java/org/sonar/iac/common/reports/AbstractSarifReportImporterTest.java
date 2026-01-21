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
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.event.Level;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.ExternalIssue;
import org.sonar.api.rules.RuleType;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.iac.common.testing.IacCommonAssertions;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

import static java.util.stream.Stream.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.addFileToSensorContext;

class AbstractSarifReportImporterTest {

  private static final String PATH_PREFIX = "src/test/resources/sarif-report";
  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5().setLevel(Level.DEBUG);

  private SensorContextTester context;
  private final AnalysisWarningsWrapper mockAnalysisWarnings = mock(AnalysisWarningsWrapper.class);
  private final TestSarifRulesDefinition testRulesDefinition = new TestSarifRulesDefinition(SONARLINT_RUNTIME_9_9);

  @BeforeEach
  void setUp() {
    File baseDir = new File(PATH_PREFIX);
    context = SensorContextTester.create(baseDir);
    addFileToSensorContext(context, baseDir.toPath(), "test-file.yaml");
  }

  @ParameterizedTest
  @CsvSource({
    "/doesNotExist.json, Test SARIF importing: path does not seem to point to a file %s",
    "/parseError.json, Test SARIF importing: could not parse file as JSON %s",
    "/emptyRunsArray.json, Test SARIF importing: file is expected to contain a JSON array but didn't %s",
    "/missingRunsArray.json, Test SARIF importing: file is expected to contain a JSON array but didn't %s"
  })
  void shouldLogWarningForInvalidFiles(String reportPath, String expectedLog) {
    reportPath = PATH_PREFIX + reportPath;
    String path = File.separatorChar == '/' ? reportPath : Paths.get(reportPath).toString();
    File reportFile = new File(path);
    String logMessage = String.format(expectedLog, path);

    importReport(reportFile);

    assertThat(logTester.logs(Level.WARN)).containsExactly(logMessage);
    verify(mockAnalysisWarnings, times(1)).addWarning(logMessage);
  }

  @Test
  void shouldParseValidSarifReport() {
    File reportFile = new File(PATH_PREFIX + "/validSarifIssue.json");

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    IacCommonAssertions.assertThat(issue).hasRuleId("test-rule-1");
    assertThat(issue.type()).isEqualTo(RuleType.CODE_SMELL);
    assertThat(issue.severity()).isEqualTo(Severity.MAJOR);
    assertThat(issue.primaryLocation().message()).isEqualTo("This is a test issue");
    assertThat(issue.engineId()).isEqualTo("test-linter");
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldHandleUnknownRuleWithFallback() {
    File reportFile = new File(PATH_PREFIX + "/unknownRuleId.json");

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    IacCommonAssertions.assertThat(issue).hasRuleId("test.fallback");
    assertThat(issue.severity()).isEqualTo(Severity.CRITICAL);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldExtractSeverityFromSarifLevel() throws ParseException {
    JSONParser parser = new JSONParser();

    // Test error level
    JSONObject errorIssue = (JSONObject) parser.parse("{\"level\": \"error\"}");
    assertThat(AbstractSarifReportImporter.extractSeverity(errorIssue)).isEqualTo(Severity.CRITICAL);

    // Test warning level
    JSONObject warningIssue = (JSONObject) parser.parse("{\"level\": \"warning\"}");
    assertThat(AbstractSarifReportImporter.extractSeverity(warningIssue)).isEqualTo(Severity.MAJOR);

    // Test note level
    JSONObject noteIssue = (JSONObject) parser.parse("{\"level\": \"note\"}");
    assertThat(AbstractSarifReportImporter.extractSeverity(noteIssue)).isEqualTo(Severity.MINOR);

    // Test null level (should default to MAJOR)
    JSONObject nullLevelIssue = (JSONObject) parser.parse("{}");
    assertThat(AbstractSarifReportImporter.extractSeverity(nullLevelIssue)).isEqualTo(Severity.MAJOR);

    // Test unknown level (should default to MAJOR)
    JSONObject unknownLevelIssue = (JSONObject) parser.parse("{\"level\": \"unknown\"}");
    assertThat(AbstractSarifReportImporter.extractSeverity(unknownLevelIssue)).isEqualTo(Severity.MAJOR);
  }

  @Test
  void shouldGetFilePathFromSarifJson() throws ParseException {
    JSONParser parser = new JSONParser();
    String json = """
      {
        "locations": [{
          "physicalLocation": {
            "artifactLocation": {
              "uri": "path/to/file.yaml"
            }
          }
        }]
      }
      """;
    JSONObject issueJson = (JSONObject) parser.parse(json);

    String filePath = AbstractSarifReportImporter.getFilePath(issueJson);

    assertThat(filePath).isEqualTo("path/to/file.yaml");
  }

  static Stream<Arguments> shouldThrowException() {
    return of(
      arguments("{}", "Invalid JSON format: missing 'locations' array, or it is not a JSON array"),
      arguments("{\"locations\": []}", "Invalid JSON format: 'locations' array is empty"),
      arguments("{\"locations\": \"not-an-array\"}", "Invalid JSON format: missing 'locations' array, or it is not a JSON array"));
  }

  @ParameterizedTest
  @MethodSource
  void shouldThrowException(String json, String expectedMessage) throws ParseException {
    JSONParser parser = new JSONParser();
    JSONObject issueJson = (JSONObject) parser.parse(json);

    assertThatThrownBy(() -> AbstractSarifReportImporter.getPhysicalLocation(issueJson))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(expectedMessage);
  }

  @Test
  void shouldHandleFullTextRange() {
    File reportFile = new File(PATH_PREFIX + "/fullTextRange.json");

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    // The test file doesn't have enough content for the full range specified in fullTextRange.json,
    // so it falls back to a simpler range
    assertThat(issue.primaryLocation().textRange()).isNotNull();
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(2);
  }

  @Test
  void shouldHandleLineOnlyWhenColumnsNotProvided() {
    File reportFile = new File(PATH_PREFIX + "/lineOnlyRange.json");

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    // Should select entire line 3
    assertThat(issue.primaryLocation().textRange().start().line()).isEqualTo(3);
    assertThat(issue.primaryLocation().textRange().end().line()).isEqualTo(3);
  }

  @Test
  void shouldHandleMultipleIssuesInReport() {
    File reportFile = new File(PATH_PREFIX + "/multipleIssues.json");

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(3);
    verifyNoInteractions(mockAnalysisWarnings);
  }

  @Test
  void shouldLogWarningWhenSomeIssuesFailToSave() {
    File reportFile = new File(PATH_PREFIX + "/mixedValidInvalid.json");
    String expectedLog = String.format(
      "Test SARIF importing: could not save 1 out of 2 issues from %s.",
      reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings).addWarning(expectedLog);
  }

  @Test
  void shouldReportUnresolvedFilePaths() {
    File reportFile = new File(PATH_PREFIX + "/unresolvedFiles.json");
    String expectedLog = String.format(
      "Test SARIF importing: could not save 2 out of 2 issues from %s. " +
        "Some file paths could not be resolved: nonexistent1.yaml, nonexistent2.yaml",
      reportFile.getPath());

    importReport(reportFile);

    assertThat(context.allExternalIssues()).isEmpty();
    assertThat(logTester.logs(Level.WARN)).containsExactly(expectedLog);
    verify(mockAnalysisWarnings).addWarning(expectedLog);
  }

  @Test
  void shouldHandleInvalidRangeGracefully() {
    File reportFile = new File(PATH_PREFIX + "/invalidRange.json");

    importReport(reportFile);

    // Should fallback to line-only range
    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.primaryLocation().textRange()).isNotNull();
  }

  @Test
  void shouldUseCustomMessageFromSubclass() {
    TestSarifImporterWithCustomMessage importer = new TestSarifImporterWithCustomMessage(
      context, new TestSarifRulesDefinition(SONARLINT_RUNTIME_9_9), mockAnalysisWarnings);
    File reportFile = new File(PATH_PREFIX + "/validSarifIssue.json");

    importer.importReport(reportFile);

    assertThat(context.allExternalIssues()).hasSize(1);
    ExternalIssue issue = context.allExternalIssues().iterator().next();
    assertThat(issue.primaryLocation().message()).isEqualTo("Custom: This is a test issue");
  }

  private void importReport(File reportFile) {
    new TestSarifImporter(context, testRulesDefinition, mockAnalysisWarnings).importReport(reportFile);
  }
}

class TestSarifRulesDefinition extends AbstractExternalRulesDefinition {
  private final ExternalRuleLoader mockRuleLoader;

  TestSarifRulesDefinition(org.sonar.api.SonarRuntime sonarRuntime) {
    super(sonarRuntime, "Test SARIF");
    mockRuleLoader = mock(ExternalRuleLoader.class);
    when(mockRuleLoader.ruleKeys()).thenReturn(java.util.Set.of("test-rule-1", "test-rule-2"));
    when(mockRuleLoader.ruleSeverity(anyString())).thenReturn(Severity.MAJOR);
    when(mockRuleLoader.ruleType(anyString())).thenReturn(RuleType.CODE_SMELL);
    when(mockRuleLoader.ruleConstantDebtMinutes(anyString())).thenReturn(5L);
  }

  @Override
  public void define(Context context) {
    // No-op for tests
  }

  @Override
  public ExternalRuleLoader getRuleLoader() {
    return mockRuleLoader;
  }

  @Override
  public String languageKey() {
    return "yaml";
  }

  @Override
  public String repositoryKey() {
    return "test-linter";
  }

  @Override
  public String externalRulesPath() {
    return "com/sonar/l10n/yaml/rules/test-sarif";
  }
}

class TestSarifImporter extends AbstractSarifReportImporter {
  private static final String FALLBACK_ID = "test.fallback";
  private static final String LINTER_KEY = "test-linter";

  protected TestSarifImporter(SensorContext context, AbstractExternalRulesDefinition rulesDefinition,
    AnalysisWarningsWrapper analysisWarnings) {
    super(context, rulesDefinition, analysisWarnings, "Test SARIF importing: ");
  }

  @Override
  protected String getFallbackId() {
    return FALLBACK_ID;
  }

  @Override
  protected String getLinterKey() {
    return LINTER_KEY;
  }
}

class TestSarifImporterWithCustomMessage extends TestSarifImporter {

  protected TestSarifImporterWithCustomMessage(SensorContext context, AbstractExternalRulesDefinition rulesDefinition,
    AnalysisWarningsWrapper analysisWarnings) {
    super(context, rulesDefinition, analysisWarnings);
  }

  @Override
  protected String getMessageFor(String ruleId, String providedMessage) {
    return "Custom: " + providedMessage;
  }
}
