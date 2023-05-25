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
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ParameterDeclarationImplTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseMinimalParameter() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "      \"type\": \"bool\"",
      "    }",
      "  }",
      "}");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.identifier().value()).isEqualTo("enabledForDeployment");
    assertThat(parameter.type()).isEqualTo(ParameterType.BOOL);

    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  void shouldParseFullParameters() {
    String code = code("{",
      "    \"parameters\": {",
      "        \"exampleParam\": {",
      "            \"type\": \"string\",",
      "            \"defaultValue\": \"a\",",
      "            \"minValue\": 7,",
      "            \"maxValue\": 90,",
      "            \"minLength\": 1,",
      "            \"maxLength\": 10,",
      "            \"allowedValues\": [",
      "                \"A\",",
      "                \"B\",",
      "                \"CCCC\"",
      "            ],",
      "            \"metadata\": {",
      "                \"description\": \"some description\"",
      "            }",
      "        }",
      "    }",
      "}");

    File tree = (File) parser.parse(code, null);

    ParameterDeclarationImpl parameter = (ParameterDeclarationImpl) tree.statements().get(0);
    assertThat(parameter.identifier().value()).isEqualTo("exampleParam");
    assertThat(parameter.type()).isEqualTo(ParameterType.STRING);
    assertThat(parameter.defaultValue().value()).isEqualTo("a");
    assertThat(parameter.minValue().value()).isEqualTo("7");
    assertThat(parameter.maxValue().value()).isEqualTo("90");
    assertThat(parameter.minLength().value()).isEqualTo("1");
    assertThat(parameter.maxLength().value()).isEqualTo("10");
    assertThat(parameter.allowedValues()).map(Expression::value).containsExactly("A", "B", "CCCC");
    assertThat(parameter.description().value()).isEqualTo("some description");
  }
}
