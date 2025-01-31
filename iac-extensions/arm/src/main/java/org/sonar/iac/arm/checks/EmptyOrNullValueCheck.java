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

import java.util.Map;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.OutputDeclaration;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.ForExpression;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6954")
public class EmptyOrNullValueCheck implements IacCheck {
  private static final String MESSAGE = "Remove this %s or complete with real code.";
  private static final Map<ArmTree.Kind, String> TYPE_TO_STRING = Map.of(
    ArmTree.Kind.NULL_LITERAL, "null %s",
    ArmTree.Kind.STRING_LITERAL, "empty string",
    ArmTree.Kind.OBJECT_EXPRESSION, "empty object",
    ArmTree.Kind.ARRAY_EXPRESSION, "empty array");

  @Override
  public void initialize(InitContext init) {
    init.register(ResourceDeclaration.class, EmptyOrNullValueCheck::checkResource);
    init.register(VariableDeclaration.class, EmptyOrNullValueCheck::checkVariable);
    init.register(OutputDeclaration.class, EmptyOrNullValueCheck::checkOutput);
  }

  private static void checkResource(CheckContext ctx, ResourceDeclaration resource) {
    for (Property property : resource.resourceProperties()) {
      if (!isResourcePropertyException(property)) {
        checkExpression(ctx, property, property.value(), "property");
      }
    }
  }

  private static void checkVariable(CheckContext ctx, VariableDeclaration variable) {
    checkExpression(ctx, variable, variable.value(), "variable");
  }

  private static void checkOutput(CheckContext ctx, OutputDeclaration output) {
    Expression outputValue = output.value();
    if (outputValue == null) {
      return;
    }

    if (outputValue.is(ArmTree.Kind.FOR_EXPRESSION)) {
      checkForOutput(ctx, (ForExpression) outputValue);
    } else {
      checkExpression(ctx, output, outputValue, "output");
    }
  }

  /**
   * Exceptions for resource properties:
   * <ul>
   *   <li>Top-level `properties` of a resource declaration</li>
   *   <li>Property `userAssignedIdentities -> {ID}`, where ID value has to be null for several resource types, and ID
   *   itself can be an arbitrary string or variable</li>
   * </ul>
   */
  private static boolean isResourcePropertyException(Property property) {
    var isTopLevelPropertiesProperty = TextUtils.isValue(property.key(), "properties").isTrue() && isEmpty(property.value());

    var isUserAssignedIdentitiesIdProperty = Optional.ofNullable(property.parent())
      .map(ArmTree::parent)
      .map(parent -> parent instanceof Property parentProperty && TextUtils.isValue(parentProperty.key(), "userAssignedIdentities").isTrue())
      .orElse(false);
    return isTopLevelPropertiesProperty || isUserAssignedIdentitiesIdProperty;
  }

  private static void checkExpression(CheckContext ctx, HasTextRange propertyToReport, Expression expression, String kind) {
    if (isEmpty(expression)) {
      ctx.reportIssue(propertyToReport, message(expression.getKind(), kind));
    } else if (expression.is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      checkObject(ctx, (ObjectExpression) expression);
    } else if (expression.is(ArmTree.Kind.ARRAY_EXPRESSION)) {
      checkArray(ctx, (ArrayExpression) expression);
    }
  }

  private static void checkObject(CheckContext ctx, ObjectExpression object) {
    for (PropertyTree property : object.properties()) {
      Tree value = property.value();
      if (value instanceof Expression expression && !isResourcePropertyException((Property) property)) {
        checkExpression(ctx, property, expression, "property");
      }
    }
  }

  private static void checkArray(CheckContext ctx, ArrayExpression array) {
    for (Expression expression : array.elements()) {
      if (expression instanceof ObjectExpression object) {
        checkObject(ctx, object);
      }
    }
  }

  private static void checkForOutput(CheckContext ctx, ForExpression forExpression) {
    if (forExpression.bodyExpression().is(ArmTree.Kind.OBJECT_EXPRESSION)) {
      ObjectExpression forBody = (ObjectExpression) forExpression.bodyExpression();
      for (PropertyTree property : forBody.properties()) {
        Tree value = property.value();
        if (value instanceof Expression expression) {
          checkExpression(ctx, property, expression, "output");
        }
      }
    }
  }

  private static String message(ArmTree.Kind kind, String type) {
    if (kind == ArmTree.Kind.NULL_LITERAL) {
      return MESSAGE.formatted(TYPE_TO_STRING.get(kind).formatted(type));
    } else {
      return MESSAGE.formatted(TYPE_TO_STRING.get(kind));
    }
  }

  private static boolean isEmpty(Expression expression) {
    return expression.is(ArmTree.Kind.NULL_LITERAL) || isEmptyString(expression) || isEmptyObject(expression) || isEmptyArray(expression);
  }

  private static boolean isEmptyString(Expression expression) {
    return expression.is(ArmTree.Kind.STRING_LITERAL) && ((StringLiteral) expression).value().isEmpty();
  }

  private static boolean isEmptyObject(Expression expression) {
    return expression.is(ArmTree.Kind.OBJECT_EXPRESSION) && ((ObjectExpression) expression).properties().isEmpty();
  }

  private static boolean isEmptyArray(Expression expression) {
    return expression.is(ArmTree.Kind.ARRAY_EXPRESSION) && ((ArrayExpression) expression).elements().isEmpty();
  }
}
