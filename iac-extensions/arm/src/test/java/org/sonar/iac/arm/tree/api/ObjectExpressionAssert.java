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

public class ObjectExpressionAssert extends HasTextRangeAssert<ObjectExpressionAssert, ObjectExpression> {
  private ObjectExpressionAssert(ObjectExpression objectExpression) {
    super(objectExpression, ObjectExpressionAssert.class);
  }

  public static ObjectExpressionAssert assertThat(ObjectExpression actual) {
    return new ObjectExpressionAssert(actual);
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
