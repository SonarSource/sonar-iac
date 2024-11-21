/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.checks.azure.helper;

import java.util.function.Predicate;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.utils.PredicateUtils;

import static org.sonar.iac.terraform.checks.utils.TerraformUtils.attributeAccessMatches;

public class RoleScopeHelper implements PredicateUtils {

  // Predicates for sensitive subscription scopes
  public static final String REFERENCE_SUBSCRIPTION_SCOPE_PATTERN = "data\\.azurerm_subscription\\.[^.]*(primary|current)[^.]*\\.id";
  public static final String PLAIN_SUBSCRIPTION_SCOPE_PATTERN = "^/subscriptions/[^/]+/?$";

  // Predicates for sensitive management group scopes
  public static final String REFERENCE_MANAGEMENT_GROUP_SCOPE_PATTERN = "data\\.azurerm_management_group\\.[^.]*(parent|root)[^.]*\\.id";
  public static final String PLAIN_MANAGEMENT_GROUP_SCOPE_PATTERN = "^/providers/microsoft\\.management/.+";

  private RoleScopeHelper() {
    // helper class
  }

  public static boolean isSensitiveScope(ExpressionTree scope, Predicate<String> referenceScopePredicate, Predicate<String> plainScopePredicate) {
    if (scope.is(TerraformTree.Kind.ATTRIBUTE_ACCESS)) {
      return attributeAccessMatches(scope, referenceScopePredicate).isTrue();
    } else if (scope.is(TerraformTree.Kind.TEMPLATE_EXPRESSION)) {
      return !isLimitedToResourceGroup((TemplateExpressionTree) scope)
        && containsSensitiveInterpolations((TemplateExpressionTree) scope, referenceScopePredicate);
    }
    return TextUtils.matchesValue(scope, plainScopePredicate).isTrue();
  }

  private static boolean containsSensitiveInterpolations(TemplateExpressionTree scope, Predicate<String> referenceScopePredicate) {
    return scope.parts().stream()
      .filter(TemplateInterpolationTree.class::isInstance)
      .map(interpolation -> ((TemplateInterpolationTree) interpolation).expression())
      .filter(AttributeAccessTree.class::isInstance)
      .map(AttributeAccessTree.class::cast)
      .anyMatch(interpolation -> attributeAccessMatches(interpolation, referenceScopePredicate).isTrue());
  }

  private static boolean isLimitedToResourceGroup(TemplateExpressionTree scope) {
    return scope.parts().stream()
      .filter(LiteralExprTree.class::isInstance)
      .anyMatch(part -> TextUtils.matchesValue(part, s -> s.contains("resourceGroups")).isTrue());
  }
}
