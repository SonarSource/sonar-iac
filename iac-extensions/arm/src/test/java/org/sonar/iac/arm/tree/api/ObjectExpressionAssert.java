/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
import org.sonar.iac.common.checks.PropertyUtils;

public class ObjectExpressionAssert extends HasTextRangeAssert<ObjectExpressionAssert, ObjectExpression> {
  private ObjectExpressionAssert(ObjectExpression objectExpression) {
    super(objectExpression, ObjectExpressionAssert.class);
  }

  public static ObjectExpressionAssert assertThat(ObjectExpression actual) {
    return new ObjectExpressionAssert(actual);
  }

  public ObjectExpressionAssert containsVariableKeyValue(String key, String value) {
    Assertions.assertThat(PropertyUtils.valueIs(actual, key, tree -> ((Identifier) ((Variable) tree).identifier()).value().equals(value))).isTrue();
    return this;
  }

  public ObjectExpressionAssert containsKeyValue(String key, String value) {
    Assertions.assertThat(PropertyUtils.valueIs(actual, key, tree -> ((StringLiteral) tree).value().equals(value))).isTrue();
    return this;
  }

  public ObjectExpressionAssert hasSize(int objectSize) {
    Assertions.assertThat(actual.properties()).hasSize(objectSize);
    return this;
  }
}
