/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.LINE_OFFSET;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.parseProperty;

class NullLiteralImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseNullValue() {
    Property nullProperty = parseProperty(parser, "\"null_value\": null");
    assertThat(nullProperty.value())
      .isNullLiteral()
      .hasRange(LINE_OFFSET + 1, 14, LINE_OFFSET + 1, 18);
    assertThat(nullProperty.value().getKind()).isEqualTo(ArmTree.Kind.NULL_LITERAL);
    assertThat(nullProperty.value().children()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "NULL",
    "Null",
    ""
  })
  void shouldFailOnInvalidNumericFormat(String nullValue) {
    assertThatThrownBy(() -> parseProperty(parser, "\"invalid_null_value\": " + nullValue)).isInstanceOf(ParseException.class);
  }
}
