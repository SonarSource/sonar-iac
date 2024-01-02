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

class BooleanLiteralImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseTrueValue() {
    Property booleanProperty = parseProperty(parser, "\"bool\": true");
    assertThat(booleanProperty.value())
      .asBooleanLiteral()
      .isTrue()
      .hasRange(LINE_OFFSET + 1, 8, LINE_OFFSET + 1, 12);
    assertThat(booleanProperty.value().getKind()).isEqualTo(ArmTree.Kind.BOOLEAN_LITERAL);
    assertThat(booleanProperty.value().children()).isEmpty();
  }

  @Test
  void shouldParseFalseValue() {
    Property booleanProperty = parseProperty(parser, "\"bool\": false");
    assertThat(booleanProperty.value())
      .asBooleanLiteral()
      .isFalse()
      .hasRange(LINE_OFFSET + 1, 8, LINE_OFFSET + 1, 13);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "TRUE",
    "FALSE",
    "trUE"
  })
  void shouldFailOnInvalidBoolean(String str) {
    assertThatThrownBy(() -> parseProperty(parser, "\"invalid_boolean\": " + str)).isInstanceOf(ParseException.class);
  }
}
