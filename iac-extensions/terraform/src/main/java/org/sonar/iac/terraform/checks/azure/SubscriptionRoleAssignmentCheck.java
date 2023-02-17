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
package org.sonar.iac.terraform.checks.azure;

import java.util.function.Predicate;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper;

import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.utils.PredicateUtils.exactMatchStringPredicate;

@Rule(key = "S6387")
public class SubscriptionRoleAssignmentCheck extends AbstractResourceCheck {

  private static final String SUBSCRIPTION_MESSAGE = "Make sure assigning this role with a Subscription scope is safe here.";
  private static final String MANAGEMENT_GROUP_MESSAGE = "Make sure assigning this role with a Management Group scope is safe here.";

  private static final Predicate<String> REFERENCE_SUBSCRIPTION_SCOPE_PREDICATE = exactMatchStringPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN);
  private static final Predicate<String> REFERENCE_MANAGEMENT_GROUP_SCOPE_PREDICATE = exactMatchStringPredicate(REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_SUBSCRIPTION_SCOPE_PREDICATE = exactMatchStringPredicate(PLAIN_SUBSCRIPTION_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_MANAGEMENT_GROUP_SCOPE_PREDICATE = exactMatchStringPredicate(PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN);

  @Override
  protected void registerResourceChecks() {
    register(SubscriptionRoleAssignmentCheck::checkRoleAssignment, "azurerm_role_assignment");
  }

  private static void checkRoleAssignment(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "scope", AttributeTree.class)
      .ifPresent(scope -> {
        if (RoleScopeHelper.isSensitiveScope(scope.value(), REFERENCE_SUBSCRIPTION_SCOPE_PREDICATE, PLAIN_SUBSCRIPTION_SCOPE_PREDICATE)) {
          ctx.reportIssue(scope, SUBSCRIPTION_MESSAGE);
        } else if (RoleScopeHelper.isSensitiveScope(scope.value(), REFERENCE_MANAGEMENT_GROUP_SCOPE_PREDICATE, PLAIN_MANAGEMENT_GROUP_SCOPE_PREDICATE)) {
          ctx.reportIssue(scope, MANAGEMENT_GROUP_MESSAGE);
        }
      });
  }
}
