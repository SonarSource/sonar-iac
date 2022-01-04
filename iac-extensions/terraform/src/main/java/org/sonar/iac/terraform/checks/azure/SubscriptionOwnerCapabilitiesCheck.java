/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

@Rule(key = "S6385")
public class SubscriptionOwnerCapabilitiesCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Narrow the actions or assignable scope of this custom role.";

  // Predicates for sensitive subscription scopes
  private static final String REFERENCE_SUBSCRIPTION_SCOPE_PATTERN = "data\\.azurerm_subscription\\.[^.]*(primary|current)[^.]*\\.id";
  private static final String PLAIN_SUBSCRIPTION_SCOPE_PATTERN = "^/subscriptions/[^/]+/?";

  // Predicates for sensitive management group scopes
  private static final String REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN = "data\\.azurerm_management_group\\.[^.]*(parent|root)[^.]*\\.id";
  private static final String PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN = "^/providers/microsoft\\.management/.+";

  private static final Predicate<String> REFERENCE_SCOPE_PREDICATE = matchPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN, REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> PLAIN_SCOPE_PREDICATE = matchPredicate(PLAIN_SUBSCRIPTION_SCOPE_PATTERN, PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN);

  private static Predicate<String> matchPredicate(String subscriptionPattern, String managementGroupPattern) {
    return s -> Pattern.compile(subscriptionPattern + "|" + managementGroupPattern).matcher(s).find();
  }

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
    if (scope instanceof AttributeAccessTree) {
      return containsSensitiveScope(((AttributeAccessTree) scope));
    } else if (scope instanceof TemplateExpressionTree) {
      return templateInterpolations((TemplateExpressionTree) scope)
        .anyMatch(interpolation -> containsSensitiveScope(interpolation));
    }
    return TextUtils.matchesValue(scope, PLAIN_SCOPE_PREDICATE).isTrue();
  }

  private static boolean containsSensitiveScope(AttributeAccessTree accessTree) {
    return REFERENCE_SCOPE_PREDICATE.test(referenceToString(accessTree));
  }

  private static Stream<AttributeAccessTree> templateInterpolations(TemplateExpressionTree scope) {
    return scope.parts().stream()
      .filter(TemplateInterpolationTree.class::isInstance)
      .map(interpolation -> ((TemplateInterpolationTree) interpolation).expression())
      .filter(AttributeAccessTree.class::isInstance)
      .map(AttributeAccessTree.class::cast);
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

  private static String referenceToString(AttributeAccessTree reference) throws IllegalArgumentException {
    StringBuilder sb = new StringBuilder();
    if (reference.object() instanceof AttributeAccessTree) {
      sb.append(referenceToString((AttributeAccessTree) reference.object()));
      sb.append('.');
    } else if (reference.object() instanceof VariableExprTree) {
      sb.append(((VariableExprTree) reference.object()).value());
      sb.append('.');
    }
    sb.append(reference.attribute().value());
    return sb.toString();
  }
}
