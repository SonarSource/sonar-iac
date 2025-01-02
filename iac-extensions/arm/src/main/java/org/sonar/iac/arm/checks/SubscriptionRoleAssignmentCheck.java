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
package org.sonar.iac.arm.checks;

import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.File.Scope;
import org.sonar.iac.common.api.checks.SecondaryLocation;

@Rule(key = "S6387")
public class SubscriptionRoleAssignmentCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Make sure assigning this role with a %s is safe here.";
  private static final Map<Scope, String> SENSITIVE_SCOPE_WITH_NAME = Map.of(
    Scope.SUBSCRIPTION, "Subscription scope",
    Scope.MANAGEMENT_GROUP, "Management Group scope");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Authorization/roleAssignments", SubscriptionRoleAssignmentCheck::checkRoleAssignments);
  }

  private static void checkRoleAssignments(ContextualResource resource) {
    File file = (File) ArmTreeUtils.getRootNode(resource.tree);
    String sensitiveScope = SENSITIVE_SCOPE_WITH_NAME.get(file.targetScope());
    if (sensitiveScope != null) {
      resource.report(String.format(MESSAGE, sensitiveScope), new SecondaryLocation(file.targetScopeLiteral(), sensitiveScope));
    }
  }
}
