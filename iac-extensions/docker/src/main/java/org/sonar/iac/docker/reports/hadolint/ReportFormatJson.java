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
