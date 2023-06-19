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

public class ExpressionAssert extends HasTextRangeAssert<ExpressionAssert, Expression> {
  private ExpressionAssert(Expression expression) {
    super(expression, ExpressionAssert.class);
  }

  public static ExpressionAssert assertThat(Expression actual) {
    return new ExpressionAssert(actual);
  }

  public ArrayExpressionAssert asArrayExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.ARRAY_EXPRESSION)).isTrue();
    return ArrayExpressionAssert.assertThat((ArrayExpression) actual);
  }

  public ExpressionAssert isNullLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.NULL_LITERAL)).isTrue();
    return this;
  }

  public ObjectExpressionAssert asObjectExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();
    return ObjectExpressionAssert.assertThat(((ObjectExpression) actual));
  }

  public StringLiteralAssert asStringLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.STRING_LITERAL)).isTrue();
    return StringLiteralAssert.assertThat((StringLiteral) actual);
  }

  public NumericLiteralAssert asNumericLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.NUMERIC_LITERAL)).isTrue();
    return NumericLiteralAssert.assertThat((NumericLiteral) actual);
  }

  public BooleanLiteralAssert asBooleanLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.BOOLEAN_LITERAL)).isTrue();
    return BooleanLiteralAssert.assertThat((BooleanLiteral) actual);
  }
}
