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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sonar.orchestrator.build.SonarScanner;

class SecondaryLocationTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/secondary_location_other_file/";

  @Test
  void shouldReportSecondaryLocationOnOtherHelmFile() {
    String projectKey = "kubernetes_secondary_with_helm";
    String language = "kubernetes";
    SonarScanner sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY, language);
    sonarScanner.setProperty("sonar.internal.analysis.failFast", "true");

    ORCHESTRATOR.executeBuild(sonarScanner);
    var issues = getIssuesForRule(projectKey, "kubernetes:S6865");
    assertThat(issues).hasSize(1);
    var issue = issues.get(0);
    assertThat(issue.getLine()).isEqualTo(6);
    assertThat(issue.getMessage()).isEqualTo("Set automountServiceAccountToken to false for this specification of kind Pod.");
    var flows = issue.getFlowsList();
    assertThat(flows).hasSize(2);

    var flow1 = flows.get(0);
    var location1 = flow1.getLocations(0);
    assertThat(location1.getComponent()).isEqualTo("kubernetes_secondary_with_helm:SecondaryLocationChart/templates/automount_service_account_token_pod_linked.yaml");
    assertThat(location1.getMsg()).isEqualTo("Through this service account");

    var flow2 = flows.get(1);
    var location2 = flow2.getLocations(0);
    assertThat(location2.getComponent()).isEqualTo("kubernetes_secondary_with_helm:SecondaryLocationChart/templates/linked_account_service_token.yaml");
    assertThat(location2.getMsg()).isEqualTo("Change this setting");
  }

  @Test
  void shouldNotReportSecondaryLocationOnOtherFileWhenDisabled() {
    String projectKey = "kubernetes_external_secondary_disabled";
    String language = "kubernetes";
    SonarScanner sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY, language);
    sonarScanner.setProperty("sonar.internal.analysis.failFast", "true");
    sonarScanner.setProperty("sonar.kubernetes.internal.helm.secondaryLocationsInOtherFilesDisable.S6865", "true");

    ORCHESTRATOR.executeBuild(sonarScanner);
    var issues = getIssuesForRule(projectKey, "kubernetes:S6865");
    assertThat(issues).hasSize(1);
    var issue = issues.get(0);
    assertThat(issue.getLine()).isEqualTo(6);
    assertThat(issue.getMessage()).isEqualTo("Set automountServiceAccountToken to false for this specification of kind Pod.");
    var flows = issue.getFlowsList();
    assertThat(flows).hasSize(1);

    var flow1 = flows.get(0);
    var location1 = flow1.getLocations(0);
    assertThat(location1.getComponent()).isEqualTo("kubernetes_external_secondary_disabled:SecondaryLocationChart/templates/automount_service_account_token_pod_linked.yaml");
    assertThat(location1.getMsg()).isEqualTo("Through this service account");
  }
}
