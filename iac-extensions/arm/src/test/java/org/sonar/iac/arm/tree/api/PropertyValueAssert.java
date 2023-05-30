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
import org.sonar.iac.common.testing.TextRangeAssert;

public class PropertyValueAssert extends ArmTreeAssert<PropertyValueAssert, PropertyValue> {
  protected PropertyValueAssert(PropertyValue propertyValue) {
    super(propertyValue, PropertyValueAssert.class);
  }

  public static PropertyValueAssert assertThat(PropertyValue actual) {
    return new PropertyValueAssert(actual);
  }

  public PropertyValueAssert isExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.EXPRESSION)).isTrue();
    return this;
  }

  public PropertyValueAssert isArrayExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.ARRAY_EXPRESSION)).isTrue();
    return this;
  }

  public PropertyValueAssert isObjectExpression() {
    Assertions.assertThat(actual.is(ArmTree.Kind.OBJECT_EXPRESSION)).isTrue();
    return this;
  }

  public PropertyValueAssert hasValue(String value) {
    Assertions.assertThat(((Expression) actual).value()).isEqualTo(value);
    return this;
  }

  public PropertyValueAssert hasRange(int startLine, int startLineOffset, int endLine, int endLineOffset) {
    TextRangeAssert.assertThat(actual.textRange()).hasRange(startLine, startLineOffset, endLine, endLineOffset);
    return this;
  }
}
