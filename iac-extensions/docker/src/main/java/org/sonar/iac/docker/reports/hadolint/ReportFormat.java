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
package org.sonar.iac.docker.reports.hadolint;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public interface ReportFormat {

  static ReportFormat getFormatBasedOnReport(JSONObject issueJson) {
    return issueJson.get("engineId") != null ? new ReportFormatSonarqube() : new ReportFormatJson();
  }

  String getPath(JSONObject issueJson);

  String getRuleId(JSONObject issueJson);

  String getMessage(JSONObject issueJson);

  NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile);

  String getRuleType(JSONObject issueJson);

  String getSeverity(JSONObject issueJson);

  default int asInt(Object o) {
    // The JSON parser transforms the values to long
    return Math.toIntExact((long) o);
  }
}
