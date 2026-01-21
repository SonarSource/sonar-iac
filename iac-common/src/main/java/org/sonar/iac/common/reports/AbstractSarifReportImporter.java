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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

/**
 * Abstract base class for importing SARIF format reports.
 * SARIF (Static Analysis Results Interchange Format) is a standard format for the output of static analysis tools.
 */
public abstract class AbstractSarifReportImporter extends AbstractJsonReportImporter {

  protected AbstractSarifReportImporter(SensorContext context,
    AbstractExternalRulesDefinition externalRulesDefinition,
    AnalysisWarningsWrapper analysisWarnings,
    String warningPrefix) {
    super(context, externalRulesDefinition, analysisWarnings, warningPrefix);
  }

  /**
   * Returns the fallback rule ID to use when a rule ID from the report is not recognized.
   */
  protected abstract String getFallbackId();

  /**
   * Returns the linter key/engine ID for this SARIF importer.
   */
  protected abstract String getLinterKey();

  /**
   * Returns the message to use for the issue.
   * By default, returns the provided message from the SARIF report.
   * Subclasses can override this to customize message formatting.
   */
  protected String getMessageFor(String ruleId, String providedMessage) {
    return providedMessage;
  }

  @Override
  protected NewExternalIssue toExternalIssue(JSONObject issueJson) {
    var inputFile = inputFile(getFilePath(issueJson));
    var ruleId = (String) issueJson.get("ruleId");

    Severity severity;
    if (!externalRuleLoader.ruleKeys().contains(ruleId)) {
      // Use fallback rule for unknown rule IDs
      ruleId = getFallbackId();
      severity = extractSeverity(issueJson);
    } else {
      severity = externalRuleLoader.ruleSeverity(ruleId);
    }

    NewExternalIssue externalIssue = context.newExternalIssue()
      .ruleId(ruleId)
      .type(externalRuleLoader.ruleType(ruleId))
      .engineId(getLinterKey())
      .severity(severity)
      .remediationEffortMinutes(externalRuleLoader.ruleConstantDebtMinutes(ruleId));
    externalIssue.at(getIssueLocation(issueJson, externalIssue, inputFile, ruleId));

    return externalIssue;
  }

  /**
   * Creates a NewIssueLocation from the SARIF issue JSON.
   * Subclasses can override this to customize location handling (e.g., full text ranges vs line-only).
   */
  protected NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile, String ruleId) {
    var physicalLocation = getPhysicalLocation(issueJson);
    var region = (JSONObject) physicalLocation.get("region");

    int startLine = asInt(region.get("startLine"));
    int startColumn = region.containsKey("startColumn") ? asInt(region.get("startColumn")) : 1;
    int endLine = region.containsKey("endLine") ? asInt(region.get("endLine")) : startLine;
    int endColumn = region.containsKey("endColumn") ? asInt(region.get("endColumn")) : -1;

    TextRange range;
    try {
      if (endLine > startLine || (endLine == startLine && endColumn > startColumn)) {
        // Full range support - convert SARIF 1-based columns to SonarQube 0-based offsets
        int startOffset = startColumn - 1;
        int endOffset = endColumn > 0 ? (endColumn - 1) : inputFile.selectLine(endLine).end().lineOffset();
        range = inputFile.newRange(startLine, startOffset, endLine, endOffset);
      } else {
        // Single line or invalid range
        range = inputFile.selectLine(startLine);
      }
    } catch (Exception e) {
      // Fallback to line-only range if column-based range fails
      range = inputFile.selectLine(startLine);
    }

    var providedMessage = (String) ((JSONObject) issueJson.get("message")).get("text");
    var message = getMessageFor(ruleId, providedMessage);
    return externalIssue.newLocation()
      .message(message)
      .on(inputFile)
      .at(range);
  }

  @Override
  protected JSONArray parseFileAsArray(File reportFile) throws IOException, ParseException {
    JSONObject parsedJson;
    try (var reader = Files.newBufferedReader(reportFile.toPath())) {
      parsedJson = (JSONObject) jsonParser.parse(reader);
    }
    JSONArray runs = (JSONArray) parsedJson.get("runs");
    if (runs == null || runs.isEmpty()) {
      throw new IllegalStateException("Invalid SARIF format: missing or empty 'runs' array");
    }

    return (JSONArray) ((JSONObject) runs.get(0)).get("results");
  }

  /**
   * Extracts the file path from the SARIF issue JSON.
   */
  protected static String getFilePath(JSONObject issueJson) {
    var physicalLocation = getPhysicalLocation(issueJson);
    var artifactLocation = (JSONObject) physicalLocation.get("artifactLocation");
    return (String) artifactLocation.get("uri");
  }

  /**
   * Gets the physicalLocation from the SARIF issue.
   * SARIF reports typically only raise issues in one location (no secondary locations).
   */
  protected static JSONObject getPhysicalLocation(JSONObject issueJson) {
    var locationsObject = issueJson.get("locations");
    if (!(locationsObject instanceof JSONArray locations)) {
      throw new IllegalStateException("Invalid JSON format: missing 'locations' array, or it is not a JSON array");
    }
    if (locations.isEmpty()) {
      throw new IllegalStateException("Invalid JSON format: 'locations' array is empty");
    }
    var firstAndOnlyLocation = (JSONObject) locations.get(0);
    return (JSONObject) firstAndOnlyLocation.get("physicalLocation");
  }

  /**
   * Extracts severity from SARIF level field.
   * Maps: error → CRITICAL, warning → MAJOR, note → MINOR
   */
  protected static Severity extractSeverity(JSONObject issueJson) {
    var level = (String) issueJson.get("level");
    if (level == null) {
      return Severity.MAJOR;
    }
    return switch (level) {
      case "error" -> Severity.CRITICAL;
      case "warning" -> Severity.MAJOR;
      case "note" -> Severity.MINOR;
      default -> Severity.MAJOR;
    };
  }
}
