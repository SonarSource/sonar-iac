/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.arm.checks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.arm.checkdsl.ContextualArray;
import org.sonar.iac.arm.checkdsl.ContextualObject;
import org.sonar.iac.arm.checkdsl.ContextualProperty;
import org.sonar.iac.arm.checkdsl.ContextualResource;
import org.sonar.iac.arm.tree.ArmTreeUtils;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterDeclaration;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.tree.ArmTreeUtils.containsParameterReference;

@Rule(key = "S6656")
public class SecureValuesExposureCheck extends AbstractArmResourceCheck {
  private static final String MESSAGE = "Change this code to not use an outer expression evaluation scope in nested templates.";
  private static final String SECONDARY_MESSAGE = "This secure parameter is leaked through the deployment history.";
  private static final String RESOURCES_PROP_NAME = "resources";

  @Override
  protected void registerResourceConsumer() {
    register("Microsoft.Resources/deployments", SecureValuesExposureCheck::checkSecureParametersInNestedTemplates);
  }

  private static void checkSecureParametersInNestedTemplates(ContextualResource resource) {
    Map<String, ParameterDeclaration> sensitiveParameters = getSensitiveParameters(resource);
    if (sensitiveParameters.isEmpty()) {
      return;
    }

    ContextualObject expressionEvaluationOptions = resource.object("expressionEvaluationOptions");
    ContextualProperty scope = expressionEvaluationOptions.property("scope");
    if (scope.isAbsent() || TextUtils.isValue(scope.valueOrNull(), "Inner").isFalse()) {
      Set<String> sensitiveParameterNames = sensitiveParameters.keySet();
      List<SecondaryLocation> sensitiveParameterUsages = extractPropertyValuesFromTemplate(resource.object("template").list(RESOURCES_PROP_NAME))
        .filter(containsParameterReference(sensitiveParameterNames))
        .map(value -> new SecondaryLocation(value.textRange(), SECONDARY_MESSAGE))
        .toList();

      if (!sensitiveParameterUsages.isEmpty()) {
        scope.report(MESSAGE, sensitiveParameterUsages)
          .reportIfAbsent(MESSAGE, sensitiveParameterUsages);
        expressionEvaluationOptions.reportIfAbsent(MESSAGE, sensitiveParameterUsages);
      }
    }
  }

  static Stream<Expression> extractPropertyValuesFromTemplate(ContextualArray resources) {
    // TODO SONARIAC-1058: traversal of resources in nested templates can be improved after they are properly supported
    return resources.objects()
      .filter(ContextualTree::isPresent)
      .flatMap(ContextualObject::allPropertiesFlattened)
      .flatMap(prop -> {
        if (RESOURCES_PROP_NAME.equals(prop.name)) {
          return extractPropertyValuesFromTemplate(ContextualArray.fromPresent(prop.ctx, (ArrayExpression) prop.valueOrNull(), RESOURCES_PROP_NAME, null));
        } else {
          return Stream.of(prop.valueOrNull());
        }
      });
  }

  private static Map<String, ParameterDeclaration> getSensitiveParameters(ContextualResource resource) {
    // TODO SONARIAC-1034: use symbol table instead of accessing parameters through `FILE`
    File file = (File) ArmTreeUtils.getRootNode(resource.tree);

    return ArmTreeUtils.getParametersByNames(file)
      .entrySet()
      .stream()
      .filter(it -> it.getValue().type() == ParameterType.SECURE_OBJECT || it.getValue().type() == ParameterType.SECURE_STRING)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
