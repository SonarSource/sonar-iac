/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.checks.utils;

import java.util.function.Predicate;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.FunctionCall;
import org.sonar.iac.common.checks.TextUtils;

public class CheckUtils {

  private CheckUtils() {
    // utils class
  }

  public static Predicate<Expression> isValue(Predicate<String> predicate) {
    return expr -> TextUtils.matchesValue(expr, predicate).isTrue();
  }

  public static Predicate<Expression> isEqual(String targetString) {
    return expr -> TextUtils.matchesValue(expr, targetString::equals).isTrue();
  }

  public static Predicate<Expression> isRegexMatch(String regex) {
    return expr -> TextUtils.getValue(expr).filter(str -> str.matches(regex)).isPresent();
  }

  public static Predicate<Expression> contains(String targetString) {
    return expr -> TextUtils.matchesValue(expr, str -> str.contains(targetString)).isTrue();
  }

  public static Predicate<Expression> isTrue() {
    return expr -> expr.is(ArmTree.Kind.BOOLEAN_LITERAL) && ((BooleanLiteral) expr).value();
  }

  public static Predicate<Expression> isFalse() {
    return expr -> expr.is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) expr).value();
  }

  public static Predicate<Expression> isEmptyString() {
    return expr -> TextUtils.isValue(expr, "").isTrue();
  }

  public static Predicate<Expression> isArrayWithValues() {
    return expr -> expr.is(ArmTree.Kind.ARRAY_EXPRESSION) && !((ArrayExpression) expr).elements().isEmpty();
  }

  public static Predicate<Expression> isEmptyArray() {
    return expr -> expr.is(ArmTree.Kind.ARRAY_EXPRESSION) && ((ArrayExpression) expr).elements().isEmpty();
  }

  public static Predicate<Expression> isEmptyObject() {
    return expr -> expr.is(ArmTree.Kind.OBJECT_EXPRESSION) && ((ObjectExpression) expr).properties().isEmpty();
  }

  public static Predicate<Expression> isFunctionCall(String functionName) {
    // TODO SONARIAC-10038 ARM Json: parse expression in string and build the AST to be same as Bicep equivalent
    // Here we detect functionCall in two ways:
    // - in Bicep we expect a FunctionCall object
    // - in Json we expect a StringLiteral with this format: "[functionName(...)]"
    return expr -> (expr.is(ArmTree.Kind.STRING_LITERAL) && isRegexMatch("^\\[" + functionName + "\\(.*\\)\\]$").test(expr)) // ARM Json
      || (expr.is(ArmTree.Kind.FUNCTION_CALL) && ((FunctionCall) expr).name().value().equals(functionName));// ARM Bicep
  }
}
