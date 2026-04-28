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
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8680")
public class LogicAppBasicAuthCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Replace Basic or Raw authentication with Managed Identity or OAuth 2.0 for this Logic Apps %s.";
  private static final Set<String> INSECURE_AUTH_TYPES = Set.of("basic", "raw");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Logic/workflows", LogicAppBasicAuthCheck::checkBasicAuth);
  }

  private static void checkBasicAuth(ContextualResource resource) {
    var definition = resource.object("definition");
    visitOperationGroup(resource.ctx, definition.object("actions").tree, "action");
    visitOperationGroup(resource.ctx, definition.object("triggers").tree, "trigger");
  }

  private static void visitOperationGroup(CheckContext ctx, @Nullable ObjectExpression group, String operationKind) {
    if (group == null) {
      return;
    }
    for (var prop : group.properties()) {
      if (prop.value() instanceof ObjectExpression operationObj) {
        visitOperation(ctx, operationObj, operationKind);
      }
    }
  }

  private static void visitOperation(CheckContext ctx, ObjectExpression operationObj, String operationKind) {
    checkAuthentication(ctx, operationObj, operationKind);
    visitNestedActions(ctx, operationObj, operationKind);
  }

  private static void visitNestedActions(CheckContext ctx, ObjectExpression operationObj, String operationKind) {
    // Scope, If (then-branch), ForEach, Until: direct `actions` block
    visitActionsBlockOf(ctx, operationObj, operationKind);

    // If else-branch
    PropertyUtils.value(operationObj, "else", ObjectExpression.class)
      .ifPresent(elseObj -> visitActionsBlockOf(ctx, elseObj, operationKind));

    // Switch cases
    PropertyUtils.value(operationObj, "cases", ObjectExpression.class)
      .ifPresent(cases -> {
        for (var caseProp : cases.properties()) {
          if (caseProp.value() instanceof ObjectExpression caseObj) {
            visitActionsBlockOf(ctx, caseObj, operationKind);
          }
        }
      });

    // Switch default-branch
    PropertyUtils.value(operationObj, "default", ObjectExpression.class)
      .ifPresent(defaultObj -> visitActionsBlockOf(ctx, defaultObj, operationKind));

    // Parallel branches
    PropertyUtils.value(operationObj, "branches", ObjectExpression.class)
      .ifPresent(branches -> {
        for (var branchProp : branches.properties()) {
          if (branchProp.value() instanceof ObjectExpression branchObj) {
            visitActionsBlockOf(ctx, branchObj, operationKind);
          }
        }
      });
  }

  private static void visitActionsBlockOf(CheckContext ctx, ObjectExpression container, String operationKind) {
    PropertyUtils.value(container, "actions", ObjectExpression.class)
      .ifPresent(nested -> visitOperationGroup(ctx, nested, operationKind));
  }

  private static void checkAuthentication(CheckContext ctx, ObjectExpression operationObj, String operationKind) {
    PropertyUtils.value(operationObj, "inputs", ObjectExpression.class)
      .flatMap(inputs -> PropertyUtils.value(inputs, "authentication", ObjectExpression.class))
      .ifPresent(auth -> PropertyUtils.value(auth, "type").ifPresent(typeValue -> {
        if (TextUtils.matchesValue(typeValue, value -> INSECURE_AUTH_TYPES.contains(value.toLowerCase(Locale.ROOT))).isTrue()) {
          ctx.reportIssue(auth, MESSAGE.formatted(operationKind));
        }
      }));
  }
}
