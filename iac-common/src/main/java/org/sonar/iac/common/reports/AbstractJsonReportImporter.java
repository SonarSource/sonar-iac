/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.iac.common.warnings.AnalysisWarningsWrapper;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONArray;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.JSONParser;
import org.sonarsource.analyzer.commons.internal.json.simple.parser.ParseException;

public abstract class AbstractJsonReportImporter {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractJsonReportImporter.class);
  protected static final JSONParser jsonParser = new JSONParser();
  protected final ExternalRuleLoader externalRuleLoader;
  protected final SensorContext context;
  private final AnalysisWarningsWrapper analysisWarnings;
  private final String warningPrefix;
  private Set<String> unresolvedPaths;

  protected AbstractJsonReportImporter(SensorContext context,
    AbstractExternalRulesDefinition externalRulesDefinition,
    AnalysisWarningsWrapper analysisWarnings,
    String warningPrefix) {
    this.externalRuleLoader = externalRulesDefinition.getRuleLoader();
    this.context = context;
    this.analysisWarnings = analysisWarnings;
    this.warningPrefix = warningPrefix;
  }

  public void importReport(File reportFile) {
    if (!reportFile.isFile()) {
      var message = String.format("path does not seem to point to a file %s", reportFile.getPath());
      logWarning(message);
      return;
    } else {
      LOG.info("{}Importing external report from: {}", warningPrefix, reportFile.getPath());
    }

    parseJson(reportFile).forEach(issuesJson -> {
      unresolvedPaths = new LinkedHashSet<>();
      int failedToSaveIssues = saveIssues(issuesJson);
      if (failedToSaveIssues > 0) {
        addWarning(reportFile.getPath(), issuesJson.size(), failedToSaveIssues);
      }
    });
  }

  protected List<JSONArray> parseJson(File reportFile) {
    JSONArray issuesJson = null;
    try {
      issuesJson = parseFileAsArray(reportFile);
    } catch (IOException e) {
      var message = String.format("could not read report file %s", reportFile.getPath());
      logWarning(message);
    } catch (ParseException e) {
      var message = String.format("could not parse file as JSON %s", reportFile.getPath());
      logWarning(message);
    } catch (RuntimeException e) {
      var message = String.format("file is expected to contain a JSON array but didn't %s", reportFile.getPath());
      logWarning(message);
    }
    if (issuesJson == null) {
      return Collections.emptyList();
    }
    return List.of(issuesJson);
  }

  protected JSONArray parseFileAsArray(File reportFile) throws IOException, ParseException {
    return (JSONArray) jsonParser.parse(Files.newBufferedReader(reportFile.toPath()));
  }

  protected int saveIssues(JSONArray issuesJson) {
    var failedToSaveIssues = 0;
    for (Object issueJson : issuesJson) {
      try {
        NewExternalIssue externalIssue = toExternalIssue((JSONObject) issueJson);
        externalIssue.save();
      } catch (RuntimeException e) {
        LOG.debug("failed to save issue", e);
        failedToSaveIssues++;
      }
    }
    return failedToSaveIssues;
  }

  protected abstract NewExternalIssue toExternalIssue(JSONObject issueJson);

  protected void logWarning(String message) {
    String warning = warningPrefix + message;
    LOG.warn(warning);
    analysisWarnings.addWarning(warning);
  }

  protected void addUnresolvedPath(String path) {
    unresolvedPaths.add(path);
  }

  protected static int asInt(Object o) {
    // The JSON parser transforms the values to long
    return Math.toIntExact((long) o);
  }

  protected InputFile inputFile(@Nullable String filename) {
    if (filename == null) {
      throw new ReportImporterException("Empty path");
    }
    FilePredicates predicates = context.fileSystem().predicates();
    var inputFile = context.fileSystem().inputFile(predicates.or(
      predicates.hasAbsolutePath(filename),
      predicates.hasRelativePath(filename)));

    if (inputFile == null) {
      addUnresolvedPath(filename);
      throw new ReportImporterException(String.format("The file: %s could not be resolved", filename));
    }

    return inputFile;
  }

  private void addWarning(String path, int total, int failed) {
    var sb = new StringBuilder(String.format("could not save %d out of %d issues from %s.", failed, total, path));
    if (!unresolvedPaths.isEmpty()) {
      sb.append(" Some file paths could not be resolved: ");
      sb.append(unresolvedPaths.stream().limit(2).collect(Collectors.joining(", ")));
      sb.append(unresolvedPaths.size() > 2 ? ", ..." : "");
    }
    logWarning(sb.toString());
  }
}
