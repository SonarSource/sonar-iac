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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8684")
public class LogicAppNestingDepthCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Refactor this Logic App workflow to reduce control action nesting depth from %d to at most %d.";
  private static final String SECONDARY_MESSAGE = "Enclosing control action.";
  private static final Set<String> CONTROL_ACTION_TYPES = Set.of("If", "Switch", "Foreach", "Until", "Scope");
  private static final int DEFAULT_MAX = 3;

  @RuleProperty(
    key = "max",
    defaultValue = "" + DEFAULT_MAX,
    description = "Maximum allowed nesting depth of control actions")
  public int max = DEFAULT_MAX;

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Logic/workflows", this::checkNestingDepth);
  }

  private void checkNestingDepth(ContextualResource resource) {
    ContextualObject actions = resource.object("definition").object("actions");
    if (actions.tree == null) {
      return;
    }
    walkActions(actions.ctx, actions.tree, Collections.emptyList());
  }

  private void walkActions(CheckContext ctx, ObjectExpression actionsObj, List<Property> path) {
    for (var prop : actionsObj.properties()) {
      if (prop instanceof Property property
        && property.value() instanceof ObjectExpression actionObj
        && isControlAction(actionObj)) {
        int newDepth = path.size() + 1;
        if (newDepth > max) {
          List<SecondaryLocation> secondaries = path.stream()
            .map(p -> new SecondaryLocation(p.key(), SECONDARY_MESSAGE))
            .toList();
          ctx.reportIssue(property, MESSAGE.formatted(newDepth, max), secondaries);
        } else {
          List<Property> newPath = new ArrayList<>(path);
          newPath.add(property);
          recurseIntoAction(ctx, actionObj, newPath);
        }
      }
    }
  }

  private void recurseIntoAction(CheckContext ctx, ObjectExpression actionObj, List<Property> path) {
    // Recurse into "actions" block
    getActions(actionObj)
      .ifPresent(actions -> walkActions(ctx, actions, path));

    // For "If" actions: also check "else.actions"
    getObjectExpression(actionObj, "else")
      .flatMap(LogicAppNestingDepthCheck::getActions)
      .ifPresent(elseActions -> walkActions(ctx, elseActions, path));

    // For "Switch" actions: check "cases.<caseName>.actions" and "default.actions"
    getObjectExpression(actionObj, "cases")
      .ifPresent(casesObj -> {
        for (var caseProp : casesObj.properties()) {
          if (caseProp.value() instanceof ObjectExpression caseObj) {
            getActions(caseObj)
              .ifPresent(caseActions -> walkActions(ctx, caseActions, path));
          }
        }
      });

    getObjectExpression(actionObj, "default")
      .flatMap(LogicAppNestingDepthCheck::getActions)
      .ifPresent(defaultActions -> walkActions(ctx, defaultActions, path));
  }

  private static Optional<ObjectExpression> getActions(ObjectExpression parent) {
    return getObjectExpression(parent, "actions");
  }

  private static Optional<ObjectExpression> getObjectExpression(Tree from, String key) {
    return PropertyUtils.value(from, key, ObjectExpression.class);
  }

  private static boolean isControlAction(ObjectExpression actionObj) {
    return PropertyUtils.value(actionObj, "type")
      .flatMap(TextUtils::getValue)
      .map(CONTROL_ACTION_TYPES::contains)
      .orElse(false);
  }
}
