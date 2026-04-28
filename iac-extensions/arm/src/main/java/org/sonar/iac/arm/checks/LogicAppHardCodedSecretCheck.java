/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8681")
public class LogicAppHardCodedSecretCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Do not hard-code secrets in workflow definitions. Use parameters referencing Azure Key Vault instead.";

  private static final Set<String> SENSITIVE_HEADERS = Set.of(
    "authorization",
    "api-key",
    "x-api-key",
    "ocp-apim-subscription-key");

  private static final Set<String> SENSITIVE_AUTH_PROPERTIES = Set.of(
    "password",
    "secret",
    "pfx",
    "value");

  private static final Set<String> SAFE_EXPRESSION_MARKERS = Set.of(
    "@parameters(",
    "@{",
    "[parameters(",
    "[variables(",
    "[concat(",
    "[reference(",
    "[listKeys(");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Logic/workflows", LogicAppHardCodedSecretCheck::checkWorkflow);
    register("Microsoft.Web/connections", LogicAppHardCodedSecretCheck::checkConnection);
  }

  private static void checkWorkflow(ContextualResource resource) {
    var definition = resource.object("definition");
    visitOperationGroup(resource.ctx, definition.object("actions").tree);
    visitOperationGroup(resource.ctx, definition.object("triggers").tree);
  }

  private static void checkConnection(ContextualResource resource) {
    var parameterValues = resource.object("parameterValues");
    if (parameterValues.tree == null) {
      return;
    }
    for (var prop : parameterValues.tree.properties()) {
      if (prop instanceof Property property
        && SENSITIVE_AUTH_PROPERTIES.contains(property.key().value().toLowerCase(Locale.ROOT))
        && isHardCodedLiteral(property)) {
        resource.ctx.reportIssue(property, MESSAGE);
      }
    }
  }

  private static void visitOperationGroup(CheckContext ctx, @Nullable ObjectExpression group) {
    if (group == null) {
      return;
    }
    for (var prop : group.properties()) {
      if (prop.value() instanceof ObjectExpression operationObj) {
        visitOperation(ctx, operationObj);
      }
    }
  }

  private static void visitOperation(CheckContext ctx, ObjectExpression operationObj) {
    checkActionInputs(ctx, operationObj);
    visitNestedActions(ctx, operationObj);
  }

  private static void visitNestedActions(CheckContext ctx, ObjectExpression operationObj) {
    // Scope, If (then-branch), ForEach, Until: direct `actions` block
    visitActionsBlockOf(ctx, operationObj);

    // If else-branch
    PropertyUtils.value(operationObj, "else", ObjectExpression.class)
      .ifPresent(elseObj -> visitActionsBlockOf(ctx, elseObj));

    // Switch cases
    PropertyUtils.value(operationObj, "cases", ObjectExpression.class)
      .ifPresent(cases -> {
        for (var caseProp : cases.properties()) {
          if (caseProp.value() instanceof ObjectExpression caseObj) {
            visitActionsBlockOf(ctx, caseObj);
          }
        }
      });

    // Switch default-branch
    PropertyUtils.value(operationObj, "default", ObjectExpression.class)
      .ifPresent(defaultObj -> visitActionsBlockOf(ctx, defaultObj));

    // Parallel branches
    PropertyUtils.value(operationObj, "branches", ObjectExpression.class)
      .ifPresent(branches -> {
        for (var branchProp : branches.properties()) {
          if (branchProp.value() instanceof ObjectExpression branchObj) {
            visitActionsBlockOf(ctx, branchObj);
          }
        }
      });
  }

  private static void visitActionsBlockOf(CheckContext ctx, ObjectExpression container) {
    PropertyUtils.value(container, "actions", ObjectExpression.class)
      .ifPresent(nested -> visitOperationGroup(ctx, nested));
  }

  private static void checkActionInputs(CheckContext ctx, ObjectExpression actionObj) {
    PropertyUtils.value(actionObj, "inputs", ObjectExpression.class).ifPresent(inputs -> {
      checkProperties(ctx, inputs, "headers", SENSITIVE_HEADERS);
      checkProperties(ctx, inputs, "authentication", SENSITIVE_AUTH_PROPERTIES);
    });
  }

  private static void checkProperties(CheckContext ctx, ObjectExpression inputs, String key, Set<String> sensitiveKeys) {
    PropertyUtils.value(inputs, key, ObjectExpression.class).ifPresent(value -> {
      for (var prop : value.properties()) {
        if (prop instanceof Property authProp
          && sensitiveKeys.contains(authProp.key().value().toLowerCase(Locale.ROOT))
          && isHardCodedLiteral(authProp)) {
          ctx.reportIssue(authProp, MESSAGE);
        }
      }
    });
  }

  private static boolean isHardCodedLiteral(Property property) {
    return TextUtils.getValue(property.value())
      .map(value -> SAFE_EXPRESSION_MARKERS.stream().noneMatch(value::contains))
      .orElse(false);
  }
}
