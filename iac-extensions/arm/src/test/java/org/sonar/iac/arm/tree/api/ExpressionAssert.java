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
package org.sonar.iac.arm.tree.api;

import org.assertj.core.api.Assertions;
import org.sonar.iac.arm.ArmAssertions;

public class ExpressionAssert extends HasTextRangeAssert<ExpressionAssert, Expression> {
  private ExpressionAssert(Expression expression) {
    super(expression, ExpressionAssert.class);
  }

  public static ExpressionAssert assertThat(Expression actual) {
    return new ExpressionAssert(actual);
  }

  public ArrayExpressionAssert asArrayExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.ARRAY_EXPRESSION))
      .overridingErrorMessage("Expected Array Expression, but it is kind: " + actual.getKind())
      .isTrue();
    return ArrayExpressionAssert.assertThat((ArrayExpression) actual);
  }

  public ExpressionAssert isNullLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.NULL_LITERAL))
      .overridingErrorMessage("Expected Null Literal, but it is kind: " + actual.getKind())
      .isTrue();
    return this;
  }

  public ObjectExpressionAssert asObjectExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.OBJECT_EXPRESSION))
      .overridingErrorMessage("Expected Object Expression, but it is kind: " + actual.getKind())
      .isTrue();
    return ObjectExpressionAssert.assertThat(((ObjectExpression) actual));
  }

  public StringLiteralAssert asStringLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.STRING_LITERAL))
      .overridingErrorMessage("Expected String Literal, but it is kind: " + actual.getKind())
      .isTrue();
    return StringLiteralAssert.assertThat((StringLiteral) actual);
  }

  public IdentifierAssert asIdentifier() {
    Assertions.assertThat(actual.is(ArmTree.Kind.IDENTIFIER))
      .overridingErrorMessage("Expected Identifier, but it is kind: " + actual.getKind())
      .isTrue();
    return IdentifierAssert.assertThat((Identifier) actual);
  }

  public IdentifierAssert asWrappedIdentifier() {
    Assertions.assertThat(actual)
      .overridingErrorMessage("Expected HasIdentifier, but it is: " + actual.getClass().getSimpleName())
      .isInstanceOf(HasIdentifier.class);
    return ArmAssertions.assertThat(((HasIdentifier) actual).identifier()).asIdentifier();
  }

  public NumericLiteralAssert asNumericLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.NUMERIC_LITERAL))
      .overridingErrorMessage("Expected Numeric Literal, but it is kind: " + actual.getKind())
      .isTrue();
    return NumericLiteralAssert.assertThat((NumericLiteral) actual);
  }

  public BooleanLiteralAssert asBooleanLiteral() {
    Assertions.assertThat(actual.is(ArmTree.Kind.BOOLEAN_LITERAL))
      .overridingErrorMessage("Expected Boolean Literal, but it is kind: " + actual.getKind())
      .isTrue();
    return BooleanLiteralAssert.assertThat((BooleanLiteral) actual);
  }
}
