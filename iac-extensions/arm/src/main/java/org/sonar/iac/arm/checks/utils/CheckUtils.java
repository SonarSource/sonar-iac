/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.FunctionCall;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.bicep.MemberExpression;
import org.sonar.iac.arm.tree.api.bicep.expression.UnaryExpression;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.arm.tree.ArmTreeUtils.retrieveIdentifierOrExpression;

public final class CheckUtils {
  private static final int VERSION_FORMAT_LENGTH = "YYYY-MM-DD".length();

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

  public static Predicate<Expression> containsRecursively(String targetString) {
    return expr -> containsRecursively(expr, targetString);
  }

  private static boolean containsRecursively(ArmTree tree, String targetString) {
    return TextUtils.matchesValue(tree, str -> str.contains(targetString)).isTrue()
      || tree.children().stream().anyMatch(child -> containsRecursively((ArmTree) child, targetString));
  }

  public static Predicate<Expression> isTrue() {
    return expr -> expr.is(ArmTree.Kind.BOOLEAN_LITERAL) && ((BooleanLiteral) expr).value();
  }

  public static Predicate<Expression> isFalse() {
    return expr -> expr.is(ArmTree.Kind.BOOLEAN_LITERAL) && !((BooleanLiteral) expr).value();
  }

  public static Predicate<Expression> isNull() {
    return expr -> expr.is(ArmTree.Kind.NULL_LITERAL);
  }

  public static Predicate<Expression> isBlankString() {
    return expr -> TextUtils.matchesValue(retrieveIdentifierOrExpression(expr), String::isBlank).isTrue();
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
    return expr -> expr.is(ArmTree.Kind.FUNCTION_CALL) && ((FunctionCall) expr).name().value().equals(functionName);
  }

  /**
   * Detect if the provided expression is a call to a specific function combined with access to a specific property.
   * Example: myFunc().myProp
   */
  public static Predicate<Expression> isFunctionCallWithPropertyAccess(String functionName, String propertyName) {
    // We expect a MemberExpression with the following attributes:
    // (separatingToken=".", memberAccess=FunctionCall, expression={"propertyName" expression})
    return expr -> {
      if (expr instanceof MemberExpression memberExpression) {
        return memberExpression.separatingToken().value().equals(".")
          && isFunctionCall(functionName).test(memberExpression.memberAccess())
          && TextUtils.isValue(memberExpression.expression(), propertyName).isTrue();
      }
      return false;
    };
  }

  public static Predicate<Expression> inCollection(Collection<String> collection) {
    return expr -> TextUtils.matchesValue(expr, collection::contains).isTrue();
  }

  public static Double asNumericValueOrNull(ArmTree expr) {
    if (expr.is(ArmTree.Kind.NUMERIC_LITERAL)) {
      return ((NumericLiteral) expr).asDouble();
    } else if (expr.is(ArmTree.Kind.UNARY_EXPRESSION)) {
      UnaryExpression unaryExpression = (UnaryExpression) expr;
      if (unaryExpression.expression().is(ArmTree.Kind.NUMERIC_LITERAL)) {
        double factor = unaryExpression.operator().value().equals("-") ? -1 : 1;
        return ((NumericLiteral) unaryExpression.expression()).asDouble() * factor;
      }
    }
    return null;
  }

  public static Predicate<Expression> isVersionNewerOrEqualThan(String version) {
    // Usually, ARM versions are in the format "YYYY-MM-DD", so comparing them lexicographically yields the correct result.
    // They can also have a suffix like `-preview`; for these versions we assume they are not greater than the same version without the suffix.
    var comparator = Comparator.<String, String>comparing(v -> v.substring(0, VERSION_FORMAT_LENGTH)).reversed()
      .thenComparingInt(String::length).reversed();

    return expr -> TextUtils.matchesValue(expr, v -> v.length() >= VERSION_FORMAT_LENGTH && comparator.compare(v, version) >= 0).isTrue();
  }
}
