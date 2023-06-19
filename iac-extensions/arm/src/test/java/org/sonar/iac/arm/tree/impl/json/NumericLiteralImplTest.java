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

class NumericLiteralImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseNumericValue() {
    Property numericProperty = parseProperty(parser, "\"numeric\": 7");
    assertThat(numericProperty.value())
      .asNumericLiteral()
      .hasValue(7)
      .hasRange(LINE_OFFSET + 1, 11, LINE_OFFSET + 1, 12);
    assertThat(numericProperty.value().getKind()).isEqualTo(ArmTree.Kind.NUMERIC_LITERAL);
    assertThat(numericProperty.value().children()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "0",
    "0.5",
    ".5", // Not JSON valid but OK for our parser
    "+20",
    "-20",
    "1.0E+2",
    "1.0E-2"
  })
  void shouldParseAllNumericFormat(String numeric) {
    Property numericProperty = parseProperty(parser, "\"numeric\": " + numeric);
    assertThat(numericProperty.value()).asNumericLiteral();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "5-3",
    "--3",
    "0,5",
    "1'000"
  })
  void shouldFailOnInvalidNumericFormat(String numeric) {
    assertThatThrownBy(() -> parseProperty(parser, "\"numeric\": " + numeric)).isInstanceOf(ParseException.class);
  }
}
