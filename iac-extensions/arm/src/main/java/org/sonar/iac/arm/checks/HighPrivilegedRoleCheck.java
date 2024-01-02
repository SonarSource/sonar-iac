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

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.Expression;

import static org.sonar.iac.arm.checks.utils.CheckUtils.containsRecursively;

@Rule(key = "S6381")
public class HighPrivilegedRoleCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Make sure that assigning the %s role is safe here.";
  private static final String CONTRIBUTOR_GUID = "b24988ac-6180-42a0-ab88-20f7382dd24c";
  private static final String OWNER_GUID = "8e3af657-a8ff-443c-a75c-2fe8c4bcb635";
  private static final String USER_ACCESS_ADMINISTRATOR_GUID = "18d7d88d-d35e-4fb5-a5c3-7773c20a72d9";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Authorization/roleAssignments", checkHighPrivilegedRole());
  }

  private static Consumer<ContextualResource> checkHighPrivilegedRole() {
    return resource -> resource.property("roleDefinitionId")
      .reportIf(isContributor(), String.format(MESSAGE, "Contributor"))
      .reportIf(isOwner(), String.format(MESSAGE, "Owner"))
      .reportIf(isUserAccessAdministrator(), String.format(MESSAGE, "User Access Administrator"));
  }

  private static Predicate<Expression> isContributor() {
    return containsRecursively(CONTRIBUTOR_GUID);
  }

  private static Predicate<Expression> isOwner() {
    return containsRecursively(OWNER_GUID);
  }

  private static Predicate<Expression> isUserAccessAdministrator() {
    return containsRecursively(USER_ACCESS_ADMINISTRATOR_GUID);
  }
}
