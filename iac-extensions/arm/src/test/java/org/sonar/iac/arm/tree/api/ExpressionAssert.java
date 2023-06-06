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
package org.sonar.iac.arm.tree.api;

import org.assertj.core.api.Assertions;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.testing.TextRangeAssert;

public class ExpressionAssert extends ArmTreeAssert<ExpressionAssert, Expression> {
  protected ExpressionAssert(Expression expression) {
    super(expression, ExpressionAssert.class);
  }

  public static ExpressionAssert assertThat(Expression actual) {
    return new ExpressionAssert(actual);
  }

  public ExpressionAssert isKind(ArmTree.Kind... kinds) {
    Assertions.assertThat(actual.is(kinds)).isTrue();
    return this;
  }

  public ExpressionAssert isLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.STRING_LITERAL, ArmTree.Kind.BOOLEAN_LITERAL, ArmTree.Kind.NUMERIC_LITERAL, ArmTree.Kind.NULL_LITERAL)).isTrue();
    return this;
  }

  public ExpressionAssert isArrayExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.ARRAY_EXPRESSION)).isTrue();
    return this;
  }

  public ExpressionAssert isObjectExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();
    return this;
  }

  public ExpressionAssert hasValue(String value) {
    Assertions.assertThat(((StringLiteral) actual).value()).isEqualTo(value);
    return this;
  }

  public ExpressionAssert hasValue(boolean value) {
    Assertions.assertThat(((BooleanLiteral) actual).value()).isEqualTo(value);
    return this;
  }

  public ExpressionAssert hasValue(float value) {
    Assertions.assertThat(((NumericLiteral) actual).value()).isEqualTo(value);
    return this;
  }

  public ExpressionAssert hasArrayExpressionValues(String... values) {
    ArrayExpression arrayExpression = (ArrayExpression) actual;
    Assertions.assertThat(arrayExpression.elements()).hasSize(values.length);
    for (int i = 0; i < values.length; i++) {
      assertThat(arrayExpression.elements().get(i)).isLiteral();
      String value = values[i];
      String arrayValue = ((StringLiteral) arrayExpression.elements().get(i)).value();
      Assertions.assertThat(value).isEqualTo(arrayValue);
    }
    return this;
  }

  public ExpressionAssert hasObjectSize(int objectSize) {
    ObjectExpression objectExpression = (ObjectExpression) actual;
    Assertions.assertThat(objectExpression.properties()).hasSize(objectSize);
    return this;
  }

  public ExpressionAssert hasObjectExpression(String key, String value) {
    Assertions.assertThat(PropertyUtils.valueIs(actual, key, tree -> ((StringLiteral) tree).value().equals(value))).isTrue();
    return this;
  }

  public ExpressionAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    TextRangeAssert.assertThat(actual.textRange()).hasRange(startLine, startLineOffset, endLine, endLineOffset);
    return this;
  }
}
