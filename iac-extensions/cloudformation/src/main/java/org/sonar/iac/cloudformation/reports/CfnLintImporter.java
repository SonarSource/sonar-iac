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
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.iac.cloudformation.plugin.CfnLintRulesDefinition;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public class CfnLintImporter {
  private static final Logger LOG = Loggers.get(CfnLintImporter.class);
  private static final JSONParser jsonParser = new JSONParser();

  public static final String LINE_NUMBER_KEY = "LineNumber";
  public static final String COLUMN_NUMBER_KEY = "ColumnNumber";
  private final SensorContext context;
  private final AnalysisWarningsWrapper analysisWarnings;
  private Set<String> unresolvedPaths;

  public CfnLintImporter(SensorContext context, AnalysisWarningsWrapper analysisWarnings) {
    this.context = context;
    this.analysisWarnings = analysisWarnings;
  }

  public void importReport(File reportFile) {
    if (!reportFile.isFile()) {
      String message = String.format("Cfn-lint report importing: path does not seem to point to a file %s", reportFile.getPath());
      logWarnAndAddUnique(analysisWarnings, message);
      return;
    }

    parseJson(reportFile).ifPresent(issuesJson -> {
      unresolvedPaths = new LinkedHashSet<>();
      int failedToSaveIssues = saveIssues(issuesJson);
      if (failedToSaveIssues > 0) {
        addWarning(reportFile.getPath(), issuesJson.size(), failedToSaveIssues);
      }
    });
  }

  private Optional<JSONArray> parseJson(File reportFile) {
    JSONArray issuesJson = null;
    try {
      issuesJson = (JSONArray) jsonParser.parse(Files.newBufferedReader(reportFile.toPath()));
    } catch (IOException e) {
      String message = String.format("Cfn-lint report importing: could not read report file %s", reportFile.getPath());
      logWarnAndAddUnique(analysisWarnings, message);
    } catch (ParseException e) {
      String message = String.format("Cfn-lint report importing: could not parse file as JSON %s", reportFile.getPath());
      logWarnAndAddUnique(analysisWarnings, message);
    } catch (RuntimeException e) {
      String message = String.format("Cfn-lint report importing: file is expected to contain a JSON array but didn't %s", reportFile.getPath());
      logWarnAndAddUnique(analysisWarnings, message);
    }
    return Optional.ofNullable(issuesJson);
  }

  private int saveIssues(JSONArray issuesJson) {
    int failedToSaveIssues = 0;
    for (Object issueJson : issuesJson) {
      try {
        saveIssue((JSONObject) issueJson);
      } catch (RuntimeException e) {
        LOG.debug("Cfn-lint report importing: failed to save issue", e);
        failedToSaveIssues++;
      }
    }
    return failedToSaveIssues;
  }

  private void saveIssue(JSONObject issueJson) {
    String path = (String) issueJson.get("Filename");
    FilePredicates predicates = context.fileSystem().predicates();
    InputFile inputFile = context.fileSystem().inputFile(predicates.or(
      predicates.hasAbsolutePath(path),
      predicates.hasRelativePath(path)));

    if (inputFile == null) {
      unresolvedPaths.add(path);
    }

    Objects.requireNonNull(inputFile);
    String ruleId = (String) ((JSONObject) issueJson.get("Rule")).get("Id");
    if (!CfnLintRulesDefinition.RULE_LOADER.ruleKeys().contains(ruleId)) {
      ruleId = "cfn-lint.fallback";
    }

    NewExternalIssue externalIssue = context.newExternalIssue()
      .ruleId(ruleId)
      .type(CfnLintRulesDefinition.RULE_LOADER.ruleType(ruleId))
      .engineId(CfnLintRulesDefinition.LINTER_KEY)
      .severity(CfnLintRulesDefinition.RULE_LOADER.ruleSeverity(ruleId))
      .remediationEffortMinutes(CfnLintRulesDefinition.RULE_LOADER.ruleConstantDebtMinutes(ruleId));
    externalIssue.at(getIssueLocation(issueJson, externalIssue, inputFile));

    externalIssue.save();
  }

  private void addWarning(String path, int total, int failed) {
    StringBuilder sb = new StringBuilder(String.format("Cfn-lint report importing: could not save %d out of %d issues from %s.", failed, total, path));
    if (!unresolvedPaths.isEmpty()) {
      sb.append(" Some file paths could not be resolved: ");
      sb.append(unresolvedPaths.stream().limit(2).collect(Collectors.joining(", ")));
      sb.append(unresolvedPaths.size() > 2 ? ", ..." : "");
    }
    logWarnAndAddUnique(analysisWarnings, sb.toString());
  }

  private static NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile) {
    JSONObject issueJsonLocation = (JSONObject) issueJson.get("Location");
    JSONObject start = (JSONObject) issueJsonLocation.get("Start");
    JSONObject end = (JSONObject) issueJsonLocation.get("End");

    TextRange range;
    try {
      range = inputFile.newRange(getIntOutOfJson(start.get(LINE_NUMBER_KEY)),
        getIntOutOfJson(start.get(COLUMN_NUMBER_KEY)) - 1,
        getIntOutOfJson(end.get(LINE_NUMBER_KEY)),
        getIntOutOfJson(end.get(COLUMN_NUMBER_KEY)) - 1);
    } catch (IllegalArgumentException e) {
      // as a fallback, if start and end positions with columns are not valid for the file, let's try taking just the line
      range = inputFile.selectLine(getIntOutOfJson(end.get(LINE_NUMBER_KEY)));
    }

    return externalIssue.newLocation()
      .message((String) issueJson.get("Message"))
      .on(inputFile).at(range);
  }

  private static int getIntOutOfJson(Object o) {
    // The JSON parser transforms the values to long
    return Math.toIntExact((long) o);
  }

  private static void logWarnAndAddUnique(AnalysisWarningsWrapper analysisWarnings, String message) {
    LOG.warn(message);
    analysisWarnings.addWarning(message);
  }
}
