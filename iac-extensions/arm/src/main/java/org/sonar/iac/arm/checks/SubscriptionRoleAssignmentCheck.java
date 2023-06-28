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
package org.sonar.iac.arm.checks;

import java.util.Map;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.common.api.checks.SecondaryLocation;

@Rule(key = "S6387")
public class SubscriptionRoleAssignmentCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Make sure assigning this role with a %s scope is safe here.";
  private static final Map<File.Scope, String> SENSITIVE_SCOPE_WITH_NAME = Map.of(
    File.Scope.SUBSCRIPTION, "Subscription",
    File.Scope.MANAGEMENT_GROUP, "Management Group");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Authorization/roleAssignments", SubscriptionRoleAssignmentCheck::checkRoleAssignments);
  }

  private static void checkRoleAssignments(ContextualResource resource) {
    File file = (File) resource.tree.parent();
    if (file != null) {
      String sensitiveScope = SENSITIVE_SCOPE_WITH_NAME.get(file.targetScope());
      if (sensitiveScope != null) {
        resource.report(String.format(MESSAGE, sensitiveScope), new SecondaryLocation(file.targetScopeLiteral(), sensitiveScope + " scope"));
      }
    }
  }
}
