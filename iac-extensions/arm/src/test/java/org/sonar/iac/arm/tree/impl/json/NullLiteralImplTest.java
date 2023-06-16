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
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.impl.json.PropertyTestUtils.getCode;

class NullLiteralImplTest {
  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseNullValue() {
    String code = getCode("\"null_value\": null");
    File tree = (File) parser.parse(code, null);

    Property nullProperty = ((ResourceDeclaration) tree.statements().get(0)).properties().get(0);
    assertThat(nullProperty.value()).isNullLiteral();
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "NULL",
    "Null",
    ""
  })
  void shouldFailOnInvalidNumericFormat(String nullValue) {
    String code = getCode("\"invalid_null_value\": " + nullValue);
    assertThatThrownBy(() -> parser.parse(code, null)).isInstanceOf(ParseException.class);
  }
}
