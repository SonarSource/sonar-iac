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
package org.sonarsource.iac;

import com.sonar.orchestrator.build.SonarScanner;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TFLintReportTest extends TestBase {
  private static final String PROJECT = "tflint";
  private static final String BASE_DIRECTORY = "projects/" + PROJECT + "/";

  @Test
  void import_report() {
    SonarScanner sonarScanner = getSonarScanner(PROJECT, BASE_DIRECTORY, "terraform", "aws-provider");
    // start analysis of the project
    executeBuildWithExpectedWarnings(ORCHESTRATOR, sonarScanner);

    List<Issues.Issue> issues = issuesForComponent(PROJECT);
    assertThat(issues).hasSize(9);

    // testing only the first one as sanity check
    Issues.Issue first = issues.get(0);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(first.getComponent()).isEqualTo(PROJECT + ":src/examples.tf");
    softly.assertThat(first.getRule()).isEqualTo("external_tflint:terraform_comment_syntax");
    softly.assertThat(first.getMessage()).isEqualTo("Single line comments should begin with #");
    softly.assertThat(first.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    softly.assertThat(first.getSeverity()).isEqualTo(Common.Severity.MINOR);
    softly.assertThat(first.getEffort()).isEqualTo("5min");
    softly.assertThat(first.getLine()).isEqualTo(2);
    softly.assertThat(first.getTextRange().getStartLine()).isEqualTo(2);
    softly.assertThat(first.getTextRange().getStartOffset()).isZero();
    softly.assertThat(first.getTextRange().getEndLine()).isEqualTo(3);
    softly.assertThat(first.getTextRange().getEndOffset()).isZero();
    softly.assertAll();
  }
}
