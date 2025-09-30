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

import org.junit.jupiter.api.Test;
import org.sonarsource.analyzer.commons.internal.json.simple.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

class ReportFormatSonarqubeTest {

  private static final ReportFormatSonarqube REPORT_FORMAT_SQ = new ReportFormatSonarqube();

  @Test
  void shouldThrowExceptionWhenPrimaryLocationMissingGetPath() {
    var issueJson = new JSONObject();
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> REPORT_FORMAT_SQ.getPath(issueJson));

    assertEquals("Invalid JSON format: missing 'primaryLocation' object, or it is not a JSON object",
      exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenPrimaryLocationMissingGetMessage() {
    var issueJson = new JSONObject();
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> REPORT_FORMAT_SQ.getMessage(issueJson));

    assertEquals("Invalid JSON format: missing 'primaryLocation' object, or it is not a JSON object",
      exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenPrimaryLocationMissingGetIssueLocation() {
    var issueJson = new JSONObject();
    IllegalStateException exception = assertThrows(IllegalStateException.class,
      () -> REPORT_FORMAT_SQ.getIssueLocation(issueJson, null, null));

    assertEquals("Invalid JSON format: missing 'primaryLocation' object, or it is not a JSON object",
      exception.getMessage());
  }
}
