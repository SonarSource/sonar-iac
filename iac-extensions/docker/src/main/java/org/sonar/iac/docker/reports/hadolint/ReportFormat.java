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
