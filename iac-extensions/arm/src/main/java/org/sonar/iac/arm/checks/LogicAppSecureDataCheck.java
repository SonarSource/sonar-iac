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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S8682")
public class LogicAppSecureDataCheck extends AbstractArmResourceCheck {

  private static final String MESSAGE = "Enable Secure Inputs and Outputs for this Logic Apps %s to prevent sensitive data exposure in run history.";
  private static final Set<String> REQUIRED_PROPERTIES = Set.of("inputs", "outputs");

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Logic/workflows", LogicAppSecureDataCheck::checkSecureData);
  }

  private static void checkSecureData(ContextualResource resource) {
    ContextualObject definition = resource.object("definition");
    checkOperationGroup(definition.object("actions"), "action");
    checkOperationGroup(definition.object("triggers"), "trigger");
  }

  private static void checkOperationGroup(ContextualObject group, String operationKind) {
    if (group.tree == null) {
      return;
    }

    for (var prop : group.tree.properties()) {
      if (prop.value() instanceof ObjectExpression operationObj
        && prop.key() instanceof TextTree key) {
        checkOperation(group, key.value(), operationObj, operationKind);
      }
    }
  }

  private static void checkOperation(ContextualObject group, String operationPropertyKey, ObjectExpression operationObj, String operationKind) {
    var secureDataProperties = PropertyUtils.value(operationObj, "runtimeConfiguration", ObjectExpression.class)
      .flatMap(runtimeConfig -> PropertyUtils.value(runtimeConfig, "secureData", ObjectExpression.class))
      .flatMap(secureData -> PropertyUtils.value(secureData, "properties", ArrayExpression.class));

    if (secureDataProperties.isEmpty() || !containsAllRequiredProperties(secureDataProperties.get())) {
      group.property(operationPropertyKey).report(MESSAGE.formatted(operationKind));
    }
  }

  private static boolean containsAllRequiredProperties(ArrayExpression array) {
    var found = array.elements().stream()
      .map(TextUtils::getValue)
      .flatMap(Optional::stream)
      .collect(Collectors.toSet());
    return found.containsAll(REQUIRED_PROPERTIES);
  }
}
