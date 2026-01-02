/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

public class ArrayExpressionAssert extends HasTextRangeAssert<ArrayExpressionAssert, ArrayExpression> {
  private ArrayExpressionAssert(ArrayExpression arrayExpression) {
    super(arrayExpression, ArrayExpressionAssert.class);
  }

  public static ArrayExpressionAssert assertThat(ArrayExpression actual) {
    return new ArrayExpressionAssert(actual);
  }

  public ArrayExpressionAssert containsValuesExactly(String... values) {
    Assertions.assertThat(actual.elements()).hasSize(values.length);
    for (int i = 0; i < values.length; i++) {
      ExpressionAssert.assertThat(actual.elements().get(i)).asStringLiteral().hasValue(values[i]);
    }
    return this;
  }

  public ArrayExpressionAssert isEmpty() {
    Assertions.assertThat(actual.elements()).isEmpty();
    return this;
  }
}
