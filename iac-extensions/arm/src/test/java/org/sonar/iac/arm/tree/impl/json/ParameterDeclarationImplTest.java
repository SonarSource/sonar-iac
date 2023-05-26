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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ParameterType;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.EXPRESSION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.PARAMETER_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
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
    assertThat(parameter.getKind()).isEqualTo(PARAMETER_DECLARATION);
    assertThat(parameter.is(PARAMETER_DECLARATION)).isTrue();
    assertThat(parameter.is(RESOURCE_DECLARATION)).isFalse();
    assertThat(parameter.textRange()).hasRange(3, 4, 4, 20);

    Assertions.assertThat(parameter.defaultValue()).isNull();
    assertThat(parameter.allowedValues()).isEmpty();
    Assertions.assertThat(parameter.description()).isNull();
    Assertions.assertThat(parameter.minValue()).isNull();
    Assertions.assertThat(parameter.maxValue()).isNull();
    Assertions.assertThat(parameter.minLength()).isNull();
    Assertions.assertThat(parameter.maxLength()).isNull();

    assertThat(tree.statements()).hasSize(1);
  }

  @Test
  void shouldParseFullParameter() {
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
    assertThat(parameter.defaultValue()).hasValue("a").hasKind(EXPRESSION).hasRange(5, 28, 5, 31);
    assertThat(parameter.minValue()).hasValue("7").hasKind(EXPRESSION).hasRange(6, 24, 6, 25);
    assertThat(parameter.maxValue()).hasValue("90").hasKind(EXPRESSION).hasRange(7, 24, 7, 26);
    assertThat(parameter.minLength()).hasValue("1").hasKind(EXPRESSION).hasRange(8, 25, 8, 26);
    assertThat(parameter.maxLength()).hasValue("10").hasKind(EXPRESSION).hasRange(9, 25, 9, 27);
    assertThat(parameter.allowedValues()).map(Expression::value).containsExactly("A", "B", "CCCC");
    assertThat(parameter.description()).hasValue("some description").hasKind(EXPRESSION).hasRange(16, 31, 16, 49);
    assertThat(parameter.textRange()).hasRange(3, 8, 16, 49);
  }

  @Test
  void shouldThrowExceptionWhenTypeIsMissing() {
    String code = code("{",
      "  \"parameters\": {",
      "    \"enabledForDeployment\": {",
      "    }",
      "  }",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("TODO");
  }
}
