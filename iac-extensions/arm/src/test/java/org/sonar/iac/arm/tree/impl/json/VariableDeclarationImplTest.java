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
import org.sonar.iac.arm.tree.api.VariableDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.VARIABLE_DECLARATION;
import static org.sonar.iac.common.testing.IacTestUtils.code;
import static org.sonar.iac.arm.ArmAssertions.assertThat;

class VariableDeclarationImplTest {

  private final ArmParser parser = new ArmParser();

  private String parserVariable(String name, String value) {
    return code("{",
      "  \"variables\": {",
      "    \"" + name + "\": " + value,
      "  }",
      "}");
  }

  @Test
  void shouldParseStringVariable() {
    String code = parserVariable("stringVar", "\"val\"");
    File tree = (File) parser.parse(code, null);

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration stringVar = (VariableDeclaration) tree.statements().get(0);
    assertThat(stringVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "stringVar").hasRange(3, 4, 3, 15);
    assertThat(stringVar.value()).isExpression().hasValue("val").hasRange(3, 17, 3, 22);
    assertThat(stringVar.children()).hasSize(2);
  }

  @Test
  void shouldParseArrayVariable() {
    String code = parserVariable("arrayVar", "[\"val\"]");
    File tree = (File) parser.parse(code, null);

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration arrayVar = (VariableDeclaration) tree.statements().get(0);
    assertThat(arrayVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "arrayVar").hasRange(3, 4, 3, 14);
    assertThat(arrayVar.value()).isArrayExpression().hasRange(3, 16, 3, 23);
    assertThat(arrayVar.children()).hasSize(2);
  }

  @Test
  void shouldParseObjectVariable() {
    String code = parserVariable("objectVar", "{\"key\":\"val\"}");
    File tree = (File) parser.parse(code, null);

    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration objectVar = (VariableDeclaration) tree.statements().get(0);
    assertThat(objectVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "objectVar").hasRange(3, 4, 3, 15);
    assertThat(objectVar.value()).isObjectExpression().hasRange(3, 18, 3, 29);
    assertThat(objectVar.children()).hasSize(2);
  }

  @Test
  void shouldParseVariables() {
    String code = code("{",
      "  \"variables\": {",
      "    \"stringVar\": \"val\",",
      "    \"arrayVar\": [\"val\"],",
      "    \"objectVar\": {\"key\":\"val\"},",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(3);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(1).is(VARIABLE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(2).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration stringVar = (VariableDeclaration) tree.statements().get(0);
    VariableDeclaration arrayVar = (VariableDeclaration) tree.statements().get(1);
    VariableDeclaration objectVar = (VariableDeclaration) tree.statements().get(2);

    assertThat(stringVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "stringVar").hasRange(3, 4, 3, 15);
    assertThat(stringVar.value()).isExpression().hasValue("val").hasRange(3, 17, 3, 22);
    assertThat(arrayVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "arrayVar").hasRange(4, 4, 4, 14);
    assertThat(arrayVar.value()).isArrayExpression().hasRange(4, 16, 4, 23);
    assertThat(objectVar.name()).is(ArmTree.Kind.IDENTIFIER).has("value", "objectVar").hasRange(5, 4, 5, 15);
    assertThat(objectVar.value()).isObjectExpression().hasRange(5, 18, 5, 29);

    assertThat(stringVar.children()).hasSize(2);
    assertThat(arrayVar.children()).hasSize(2);
    assertThat(objectVar.children()).hasSize(2);
  }

  @Test
  void shouldParseNoVariables() {
    String code = code("{",
      "  \"variables\": {",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).isEmpty();
  }
}
