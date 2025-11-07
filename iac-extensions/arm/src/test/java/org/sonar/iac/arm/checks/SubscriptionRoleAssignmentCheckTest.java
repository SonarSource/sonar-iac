/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
}
