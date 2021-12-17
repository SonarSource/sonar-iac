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
package org.sonar.iac.terraform.checks.azure.helper;

import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class RoleScopeHelper {

  // Predicates for sensitive subscription scopes
  public static final String REFERENCE_SUBSCRIPTION_SCOPE_PATTERN = "data\\.azurerm_subscription\\.[^.]*(primary|current)[^.]*\\.id";
  public static final String PLAIN_SUBSCRIPTION_SCOPE_PATTERN = "^/subscriptions/[^/]+/?$";

  // Predicates for sensitive management group scopes
  public static final String REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN = "data\\.azurerm_management_group\\.[^.]*(parent|root)[^.]*\\.id";
  public static final String PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN = "^/providers/microsoft\\.management/.+";

  private RoleScopeHelper() {
    // helper class
  }

  public static Predicate<String> matchPredicate(String pattern) {
    return s -> Pattern.matches(pattern, s);
  }

  public static boolean isSensitiveScope(ExpressionTree scope, Predicate<String> referenceScopePredicate, Predicate<String> plainScopePredicate) {
    if (scope instanceof AttributeAccessTree) {
      return containsSensitiveScope(((AttributeAccessTree) scope), referenceScopePredicate);
    } else if (scope instanceof TemplateExpressionTree) {
      return !isLimitedToResourceGroup((TemplateExpressionTree) scope)
       && containsSensitiveInterpolations((TemplateExpressionTree) scope, referenceScopePredicate);
    }
    return TextUtils.matchesValue(scope, plainScopePredicate).isTrue();
  }

  private static boolean containsSensitiveScope(AttributeAccessTree accessTree, Predicate<String> referenceScopePredicate) {
    return referenceScopePredicate.test(RoleScopeHelper.referenceToString(accessTree));
  }

  private static boolean containsSensitiveInterpolations(TemplateExpressionTree scope, Predicate<String> referenceScopePredicate) {
    return scope.parts().stream()
      .filter(TemplateInterpolationTree.class::isInstance)
      .map(interpolation -> ((TemplateInterpolationTree) interpolation).expression())
      .filter(AttributeAccessTree.class::isInstance)
      .map(AttributeAccessTree.class::cast)
      .anyMatch(interpolation -> containsSensitiveScope(interpolation, referenceScopePredicate));
  }

  public static String referenceToString(AttributeAccessTree reference) throws IllegalArgumentException {
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

  private static boolean isLimitedToResourceGroup(TemplateExpressionTree scope) {
    return (scope).parts().stream()
      .filter(LiteralExprTree.class::isInstance)
      .anyMatch(part -> TextUtils.matchesValue(part, s -> s.contains("resourceGroups")).isTrue());
  }
}
