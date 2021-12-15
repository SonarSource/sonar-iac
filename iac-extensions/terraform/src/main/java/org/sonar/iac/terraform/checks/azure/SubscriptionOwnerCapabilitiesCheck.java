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
import org.sonar.iac.terraform.checks.AbstractMultipleResourcesCheck;

@Rule(key = "S6385")
public class SubscriptionOwnerCapabilitiesCheck extends AbstractMultipleResourcesCheck {

  private static final String MESSAGE = "Make sure assigning this role with a %s scope is safe here.";

  // Predicates for sensitive subscription scopes
  private static final String REFERENCE_SUBSCRIPTION_SCOPE_PATTERN = "data\\.azurerm_subscription\\.[^.]*(primary|current)[^.]*\\.id";
  private static final String SUBSCRIPTION_SCOPE_PATTERN = "^/subscriptions/[^/]+/?";
  private static final Predicate<String> REFERENCE_SUBSCRIPTION_SCOPES_PREDICATE = matchPredicate(REFERENCE_SUBSCRIPTION_SCOPE_PATTERN);
  private static final Predicate<String> SUBSCRIPTION_SCOPE_PREDICATE = matchPredicate(SUBSCRIPTION_SCOPE_PATTERN);

  // Predicates for sensitive management group scopes
  private static final String REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN = "data\\.azurerm_management_group\\.[^.]*(parent|root)[^.]*\\.id";
  private static final String MANAGEMENT_GROUP_SCOPE_PATTERN = "^/providers/microsoft\\.management/.+";
  private static final Predicate<String> REFERENCE_MANAGEMENT_GROUP_PREDICATE = matchPredicate(REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN);
  private static final Predicate<String> MANAGEMENT_GROUP_SCOPE_PREDICATE = matchPredicate(MANAGEMENT_GROUP_SCOPE_PATTERN);

  private static Predicate<String> matchPredicate(String pattern) {
    return s -> Pattern.compile(pattern).matcher(s).find();
  }

  @Override
  protected void registerChecks() {
    register(SubscriptionOwnerCapabilitiesCheck::checkRoleDefinition, "azurerm_role_definition");
  }

  private static void checkRoleDefinition(CheckContext ctx, BlockTree resource) {
    if (hasOwnerPermission(resource)) {
      PropertyUtils.value(resource, "assignable_scopes", TupleTree.class).ifPresent(scopes -> checkScopes(ctx, scopes));
    }
  }

  private static void checkScopes(CheckContext ctx, TupleTree scopes) {
    for (ExpressionTree scope : scopes.elements().trees()) {
      if (hasSubscriptionScope(scope)) {
        ctx.reportIssue(scope, message("Subscription"));
        return;
      } else if (hasManagementGroupScope(scope)) {
        ctx.reportIssue(scope, message("Management Group"));
        return;
      }
    }
  }

  private static boolean hasSubscriptionScope(ExpressionTree scope) {
    return hasSensitiveScope(scope, REFERENCE_SUBSCRIPTION_SCOPES_PREDICATE, SUBSCRIPTION_SCOPE_PREDICATE);
  }

  private static boolean hasManagementGroupScope(ExpressionTree scope) {
    return hasSensitiveScope(scope, REFERENCE_MANAGEMENT_GROUP_PREDICATE, MANAGEMENT_GROUP_SCOPE_PREDICATE);
  }

  private static boolean hasSensitiveScope(ExpressionTree scope, Predicate<String> sensitiveScopeReference, Predicate<String> sensitiveScope) {
    if (scope instanceof AttributeAccessTree) {
      return containsSensitiveScope(((AttributeAccessTree) scope), sensitiveScopeReference);
    } else if (scope instanceof TemplateExpressionTree) {
      return templateInterpolations((TemplateExpressionTree) scope).anyMatch(interpolation ->
        containsSensitiveScope(interpolation, sensitiveScopeReference));
    }
    return TextUtils.matchesValue(scope, sensitiveScope).isTrue();
  }

  private static boolean containsSensitiveScope(AttributeAccessTree accessTree, Predicate<String> sensitiveScopes) {
    return sensitiveScopes.test(referenceToString(accessTree));
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

  private static String message(String scope) {
    return String.format(MESSAGE, scope);
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
