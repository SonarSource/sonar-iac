/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.extension.visitors.SensorTelemetry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.Verifier.issue;

class SubscriptionRoleAssignmentCheckTest {
  private static final SubscriptionRoleAssignmentCheck CHECK = new SubscriptionRoleAssignmentCheck();

  @Test
  void testSubscriptionJson() {
    ArmVerifier.verify("SubscriptionRoleAssignmentCheck/subscriptionDeploymentTemplate.json",
      CHECK,
      issue(7, 14, 7, 55, "Make sure assigning this role with a Subscription scope is safe here.",
        new SecondaryLocation(range(2, 13, 2, 106), "Subscription scope")),
      issue(16, 14, 16, 55),
      issue(37, 14, 37, 55));
  }

  @Test
  void testSubscriptionBicep() {
    BicepVerifier.verify("SubscriptionRoleAssignmentCheck/subscriptionDeploymentTemplate.bicep", CHECK);
  }

  @Test
  void testManagementGroupJson() {
    ArmVerifier.verify("SubscriptionRoleAssignmentCheck/managementGroupDeploymentTemplate.json",
      CHECK,
      issue(7, 14, 7, 55, "Make sure assigning this role with a Management Group scope is safe here.",
        new SecondaryLocation(range(2, 13, 2, 109), "Management Group scope")),
      issue(16, 14, 16, 55));
  }

  @Test
  void testManagementGroupBicep() {
    BicepVerifier.verify("SubscriptionRoleAssignmentCheck/managementGroupDeploymentTemplate.bicep", CHECK);
  }

  @Test
  void testDeploymentGroupJson() {
    ArmVerifier.verifyNoIssue("SubscriptionRoleAssignmentCheck/deploymentTemplate.json", CHECK);
  }

  @Test
  void testDeploymentGroupBicep() {
    BicepVerifier.verifyNoIssue("SubscriptionRoleAssignmentCheck/deploymentTemplate.bicep", CHECK);
  }

  @Test
  void shouldCollectTelemetry() {
    var check = new SubscriptionRoleAssignmentCheck();
    var sensorTelemetry = new SensorTelemetry();
    check.setSensorTelemetry(sensorTelemetry);
    BicepVerifier.verify("SubscriptionRoleAssignmentCheck/telemetry.bicep", check);

    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.azureresourcemanager.S6387.assignment_role_name.Storage_Blob_Data_Contributor", "1"),
      entry("iac.azureresourcemanager.S6387.assignment_role_id.ba92f5b4-2d11-453d-a403-e96b0029c9fe", "1"),
      entry("iac.azureresourcemanager.S6387.assignment_role_name.custom", "1"),
      entry("iac.azureresourcemanager.S6387.assignment_role_id.custom", "1"),
      entry("iac.azureresourcemanager.S6387.assignment_role_name.Reader", "1"),
      entry("iac.azureresourcemanager.S6387.assignment_role_id.acdd72a7-3385-48ef-bd42-f606fba81ae7", "1"),
      entry("iac.azureresourcemanager.S6387.principal_type.ServicePrincipal", "1"),
      entry("iac.azureresourcemanager.S6387.principal_type.User", "1"),
      entry("iac.azureresourcemanager.S6387.principal_type.Group", "1"));
  }

  @Test
  void shouldAggregateTelemetryCountsAcrossAssignments() {
    var check = new SubscriptionRoleAssignmentCheck();
    var sensorTelemetry = new SensorTelemetry();
    check.setSensorTelemetry(sensorTelemetry);
    BicepVerifier.verify("SubscriptionRoleAssignmentCheck/telemetryAggregated.bicep", check);

    // Two identical Reader/User assignments accumulate into counters greater than 1.
    assertThat(sensorTelemetry.getTelemetry()).containsOnly(
      entry("iac.azureresourcemanager.S6387.assignment_role_name.Reader", "2"),
      entry("iac.azureresourcemanager.S6387.assignment_role_id.acdd72a7-3385-48ef-bd42-f606fba81ae7", "2"),
      entry("iac.azureresourcemanager.S6387.principal_type.User", "2"));
  }
}
