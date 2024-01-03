/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.NewExternalIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

public class ReportFormatSonarqube implements ReportFormat {

  private static final String PRIMARY_LOCATION = "primaryLocation";

  @Override
  public String getPath(JSONObject issueJson) {
    return ((String) ((JSONObject) issueJson.get(PRIMARY_LOCATION)).get("filePath"));
  }

  @Override
  public String getRuleId(JSONObject issueJson) {
    return (String) issueJson.get("ruleId");
  }

  @Override
  public String getMessage(JSONObject issueJson) {
    return ((String) ((JSONObject) issueJson.get(PRIMARY_LOCATION)).get("message"));
  }

  @Override
  public NewIssueLocation getIssueLocation(JSONObject issueJson, NewExternalIssue externalIssue, InputFile inputFile) {
    JSONObject jsonTextRange = (JSONObject) ((JSONObject) issueJson.get(PRIMARY_LOCATION)).get("textRange");

    TextRange range;
    try {
      range = inputFile.newRange(
        asInt(jsonTextRange.get("startLine")),
        asInt(jsonTextRange.get("startColumn")),
        asInt(jsonTextRange.get("endLine")),
        asInt(jsonTextRange.get("endColumn")));
    } catch (IllegalArgumentException e) {
      // as a fallback, if start and end positions with columns are not valid for the file, let's try taking just the line
      range = inputFile.selectLine(asInt(jsonTextRange.get("endLine")));
    }
    return externalIssue.newLocation()
      .message(getMessage(issueJson))
      .on(inputFile)
      .at(range);
  }

  public String getRuleType(JSONObject issueJson) {
    return (String) issueJson.get("type");
  }

  public String getSeverity(JSONObject issueJson) {
    return (String) issueJson.get("severity");
  }
}
