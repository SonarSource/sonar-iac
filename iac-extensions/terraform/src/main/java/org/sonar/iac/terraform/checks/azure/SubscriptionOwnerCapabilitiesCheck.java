/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper;

import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.PLAIN_SUBSCRIPTION_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN;
import static org.sonar.iac.terraform.checks.azure.helper.RoleScopeHelper.REFERENCE_SUBSCRIPTION_SCOPE_PATTERN;

@Rule(key = "S6385")
public class SubscriptionOwnerCapabilitiesCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Narrow the actions or assignable scope of this custom role.";

  private static final Predicate<String> REFERENCE_SCOPE_PREDICATE = RoleScopeHelper.matchPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN +
    "|" + REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_SCOPE_PREDICATE = RoleScopeHelper.matchPredicate(PLAIN_SUBSCRIPTION_SCOPE_PATTERN +
    "|" + PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN);

  @Override
  protected void registerResourceChecks() {
    register(SubscriptionOwnerCapabilitiesCheck::checkRoleDefinition, "azurerm_role_definition");
  }

  private static void checkRoleDefinition(CheckContext ctx, BlockTree resource) {
    if (hasOwnerPermission(resource) && hasSensitiveScope(resource)) {
      reportResource(ctx, resource, MESSAGE);
    }
  }

  private static boolean hasSensitiveScope(BlockTree resource) {
    return PropertyUtils.value(resource, "assignable_scopes", TupleTree.class)
      .filter(scopes -> scopes.elements().trees().stream()
        .anyMatch(scope -> isSensitiveScope(scope)))
      .isPresent();
  }

  private static boolean isSensitiveScope(ExpressionTree scope) {
    return RoleScopeHelper.isSensitiveScope(scope, REFERENCE_SCOPE_PREDICATE, PLAIN_SCOPE_PREDICATE);
  }

  private static boolean hasOwnerPermission(BlockTree resource) {
    return PropertyUtils.get(resource, "permissions", BlockTree.class)
      .flatMap(permissions -> PropertyUtils.value(permissions, "actions", TupleTree.class))
      .filter(SubscriptionOwnerCapabilitiesCheck::allowAllAction)
      .isPresent();
  }

  private static boolean allowAllAction(TupleTree actions) {
    return actions.elements().trees().stream().anyMatch(action -> TextUtils.isValue(action, "*").isTrue());
  }
}
