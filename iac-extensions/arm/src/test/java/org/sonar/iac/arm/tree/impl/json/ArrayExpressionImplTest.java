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
package org.sonar.iac.arm.tree.impl.json;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Property;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.LINE_OFFSET;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.parseProperty;

class ArrayExpressionImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseArrayExpression() {
    Property arrayProperty = parseProperty(parser, "\"array_prop\": [\"val\"]");
    assertThat(arrayProperty.value())
      .asArrayExpression()
      .containsValuesExactly("val")
      .hasRange(LINE_OFFSET + 1, 14, LINE_OFFSET + 1, 21);
    assertThat(arrayProperty.value().getKind()).isEqualTo(ArmTree.Kind.ARRAY_EXPRESSION);
    assertThat(arrayProperty.value().children()).hasSize(1);
    assertThat(((ArmTree) arrayProperty.value().children().get(0)).getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
  }

  @Test
  void shouldParseEmptyArrayExpression() {
    Property arrayProperty = parseProperty(parser, "\"array_prop\": []");
    assertThat(arrayProperty.value()).asArrayExpression().isEmpty();
  }

  @Test
  void shouldParseArrayExpressionFilledWithDifferentTypesOfValues() {
    Property arrayProperty = parseProperty(parser, "\"array_prop\": [\"val\", 5, {\"key\":\"val\"}, [\"val\"]]");

    ArrayExpression array = (ArrayExpression) arrayProperty.value();
    assertThat(array.elements().get(0)).asStringLiteral().hasValue("val");
    assertThat(array.elements().get(1)).asNumericLiteral().hasValue(5);
    assertThat(array.elements().get(2)).asObjectExpression().hasSize(1).containsKeyValue("key", "val");
    assertThat(array.elements().get(3)).asArrayExpression().containsValuesExactly("val");
  }
}
