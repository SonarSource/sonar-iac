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
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

class CfnLintReportTest extends TestBase {
  private static final String PROJECT = "cfn-lint_project";
  private static final String BASE_DIRECTORY = "projects/" + PROJECT + "/";

  @Test
  void shouldImportIssuesFromCfnLintReport() {
    SonarScanner sonarScanner = getSonarScanner(PROJECT, BASE_DIRECTORY, "cloudformation", "no_rules");
    // start analysis of the project
    var logs = executeBuildAndReadLogs(ORCHESTRATOR, sonarScanner);

    assertThat(logs).contains("WARN: Cfn-lint report importing: could not save 2 out of 7 issues from");

    List<Issues.Issue> issues = issuesForComponent(PROJECT);
    assertThat(issues).hasSize(5);
    Issues.Issue issue = issues.get(0);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/template.yaml");
    softly.assertThat(issue.getRule()).isEqualTo("external_cfn-lint:E2015");
    softly.assertThat(issue.getMessage()).isEqualTo("Default should be a value within AllowedValues");
    softly.assertThat(issue.getType()).isEqualTo(Common.RuleType.BUG);
    softly.assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    softly.assertThat(issue.getEffort()).isEqualTo("0min");
    softly.assertThat(issue.getLine()).isEqualTo(13);

    issue = issues.get(1);
    softly.assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/template.yaml");
    softly.assertThat(issue.getRule()).isEqualTo("external_cfn-lint:W2001");
    softly.assertThat(issue.getMessage()).isEqualTo("Parameter UnusedParam not used.");
    softly.assertThat(issue.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    softly.assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    softly.assertThat(issue.getEffort()).isEqualTo("0min");
    softly.assertThat(issue.getLine()).isEqualTo(19);

    issue = issues.get(2);
    softly.assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/template.yaml");
    softly.assertThat(issue.getRule()).isEqualTo("external_cfn-lint:E1152");
    softly.assertThat(issue.getMessage()).isEqualTo("'ami-123456' is not a 'AWS::EC2::Image.Id'");
    softly.assertThat(issue.getType()).isEqualTo(Common.RuleType.BUG);
    softly.assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    softly.assertThat(issue.getEffort()).isEqualTo("0min");
    softly.assertThat(issue.getLine()).isEqualTo(28);

    issue = issues.get(3);
    softly.assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/template.yaml");
    softly.assertThat(issue.getRule()).isEqualTo("external_cfn-lint:E1152");
    softly.assertThat(issue.getMessage()).isEqualTo("'ami-123456' is not a 'AWS::EC2::Image.Id'");
    softly.assertThat(issue.getType()).isEqualTo(Common.RuleType.BUG);
    softly.assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    softly.assertThat(issue.getEffort()).isEqualTo("0min");
    softly.assertThat(issue.getLine()).isEqualTo(32);

    issue = issues.get(4);
    softly.assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/template.yaml");
    softly.assertThat(issue.getRule()).isEqualTo("external_cfn-lint:E3024");
    softly.assertThat(issue.getMessage())
      .isEqualTo("[{'Key': 'environment', 'Value': {'Ref': 'Environment'}}, {'Key': 'environment', 'Value': " +
        "{'Ref': 'TestEnvironment'}}] has non-unique elements for keys ['Key']");
    softly.assertThat(issue.getType()).isEqualTo(Common.RuleType.BUG);
    softly.assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    softly.assertThat(issue.getEffort()).isEqualTo("0min");
    softly.assertThat(issue.getLine()).isEqualTo(33);

    softly.assertAll();
  }

}
