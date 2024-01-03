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
package org.sonar.iac.arm.checks;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.checks.SecondaryLocation;

import static org.sonar.iac.common.testing.Verifier.issue;

class SubscriptionRoleAssignmentCheckTest {
  private static final SubscriptionRoleAssignmentCheck CHECK = new SubscriptionRoleAssignmentCheck();

  @Test
  void testSubscriptionJson() {
    ArmVerifier.verify("SubscriptionRoleAssignmentCheck/subscriptionDeploymentTemplate.json",
      CHECK,
      issue(7, 14, 7, 55, "Make sure assigning this role with a Subscription scope is safe here.",
        SecondaryLocation.secondary(2, 13, 2, 106, "Subscription scope")),
      issue(16, 14, 16, 55));
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
        SecondaryLocation.secondary(2, 13, 2, 109, "Management Group scope")),
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
}
