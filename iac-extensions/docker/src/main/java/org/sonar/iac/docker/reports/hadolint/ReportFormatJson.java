/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.docker.reports.hadolint;

import java.util.Map;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public class ReportFormatJson implements ReportFormat {

  private static final Map<String, String> severityMapping = Map.of("error", "CRITICAL", "warning", "MAJOR", "info", "MINOR", "style", "INFO");

  @Override
  public String getPath(JSONObject issueJson) {
    return (String) issueJson.get("file");
  }

  @Override
  public String getRuleId(JSONObject issueJson) {
    return (String) issueJson.get("code");
  }

  @Override
  public String getMessage(JSONObject issueJson) {
    return (String) issueJson.get("message");
  }

  @Override
  public NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile) {
    return externalIssue.newLocation()
      .message(getMessage(issueJson))
      .on(inputFile)
      .at(inputFile.selectLine(asInt(issueJson.get("line"))));
  }

  public String getRuleType(JSONObject issueJson) {
    return "CRITICAL".equals(getSeverity(issueJson)) ? "BUG" : "CODE_SMELL";
  }

  public String getSeverity(JSONObject issueJson) {
    String level = (String) issueJson.get("level");
    return severityMapping.getOrDefault(level, "INFO");
  }
}
