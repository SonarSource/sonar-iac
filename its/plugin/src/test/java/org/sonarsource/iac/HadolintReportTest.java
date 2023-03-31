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

import com.sonar.orchestrator.Orchestrator;
import com.sonar.orchestrator.build.SonarScanner;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;

import static org.assertj.core.api.Assertions.assertThat;

public class HadolintReportTest extends TestBase {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Tests.ORCHESTRATOR;
  private static final String PROJECT = "hadolint_project";
  private static final String BASE_DIRECTORY = "projects/" + PROJECT + "/";

  @Test
  public void import_report() {
    SonarScanner sonarScanner = getSonarScanner(PROJECT, BASE_DIRECTORY, "docker", "no_rules");
    // start analysis of the project
    executeBuildWithExpectedWarnings(ORCHESTRATOR, sonarScanner);

    List<Issues.Issue> issues = issuesForComponent(PROJECT);
    assertThat(issues).hasSize(1);
    Issues.Issue first = issues.get(0);
    assertThat(first.getComponent()).isEqualTo(PROJECT + ":src/test.docker");
    assertThat(first.getRule()).isEqualTo("external_hadolint:DL3007");
    assertThat(first.getMessage()).isEqualTo("Using latest is prone to errors if the image will ever update. Pin the version explicitly to a release tag");
    assertThat(first.getType()).isEqualTo(Common.RuleType.CODE_SMELL);
    assertThat(first.getSeverity()).isEqualTo(Common.Severity.MAJOR);
    assertThat(first.getEffort()).isEqualTo("5min");
    assertThat(first.getLine()).isOne();
  }

}
