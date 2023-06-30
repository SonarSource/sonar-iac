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
package org.sonarsource.iac;

import com.sonar.orchestrator.build.SonarScanner;
import com.sonar.orchestrator.junit5.OrchestratorExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class HadolintReportTest extends TestBase {
  @RegisterExtension
  public static final OrchestratorExtension ORCHESTRATOR = Tests.ORCHESTRATOR;
  private static final String PROJECT = "hadolint_project";
  private static final String BASE_DIRECTORY = "projects/" + PROJECT + "/";

  @Test
  public void import_report() {
    SonarScanner sonarScanner = getSonarScanner(PROJECT, BASE_DIRECTORY, "docker", "no_rules");
    // start analysis of the project
    executeBuildWithExpectedWarnings(ORCHESTRATOR, sonarScanner);

    List<Issues.Issue> issues = issuesForComponent(PROJECT);
    assertThat(issues).hasSize(19);

    Optional<Issues.Issue> optionalIssue = issues.stream().filter(issue -> issue.getRule().equals("external_hadolint:DL3007")).findFirst();
    assertThat(optionalIssue).isNotEmpty();
    Issues.Issue issue = optionalIssue.get();
    assertThat(issue.getComponent()).isEqualTo(PROJECT + ":src/test.docker");
    assertThat(issue.getRule()).isEqualTo("external_hadolint:DL3007");
    assertThat(issue.getMessage()).isEqualTo("Using latest is prone to errors if the image will ever update. Pin the version explicitly to a release tag");
    assertThat(issue.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(issue.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(issue.getEffort()).isEqualTo("5min");
    assertThat(issue.getLine()).isEqualTo(10);
  }

}
