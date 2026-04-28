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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8683")
public class LogicAppErrorHandlingCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Add structured error handling to this Logic App workflow using Try/Catch Scopes.";
  private static final Set<String> ERROR_STATUSES = Set.of("Failed", "TimedOut");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Logic/workflows", LogicAppErrorHandlingCheck::checkErrorHandling);
  }

  private static void checkErrorHandling(ContextualResource resource) {
    ContextualObject actions = resource.object("definition").object("actions");
    ObjectExpression actionsTree = actions.tree;
    if (actionsTree == null) {
      return;
    }

    Set<String> scopeNames = new HashSet<>();
    List<ObjectExpression> scopeBodies = new ArrayList<>();
    for (var prop : actionsTree.properties()) {
      if (prop instanceof Property property
        && property.value() instanceof ObjectExpression actionObj
        && PropertyUtils.hasValueEqual(actionObj, "type", "Scope")) {
        scopeNames.add(property.key().value());
        scopeBodies.add(actionObj);
      }
    }

    if (scopeNames.isEmpty() || scopeBodies.stream().noneMatch(scope -> isCatchScope(scope, scopeNames))) {
      actions.report(MESSAGE);
    }
  }

  private static boolean isCatchScope(ObjectExpression actionObj, Set<String> scopeNames) {
    return PropertyUtils.value(actionObj, "runAfter", ObjectExpression.class)
      .map(runAfter -> referencesWithErrorStatus(runAfter, scopeNames))
      .orElse(false);
  }

  private static boolean referencesWithErrorStatus(ObjectExpression runAfter, Set<String> scopeNames) {
    return runAfter.properties()
      .stream()
      .filter(Property.class::isInstance)
      .map(Property.class::cast)
      .anyMatch(property -> scopeNames.contains(property.key().value()) && hasErrorStatus(property));
  }

  private static boolean hasErrorStatus(Property property) {
    if (property.value() instanceof ArrayExpression array) {
      return array.elements().stream()
        .anyMatch(element -> TextUtils.matchesValue(element, ERROR_STATUSES::contains).isTrue());
    }
    return false;
  }
}
