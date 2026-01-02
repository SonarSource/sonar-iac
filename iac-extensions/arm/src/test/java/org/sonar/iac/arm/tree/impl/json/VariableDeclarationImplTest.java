/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.VariableDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.VARIABLE_DECLARATION;
import static org.sonar.iac.common.testing.IacTestUtils.code;

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
  void shouldParseVariable() {
    String code = code("{",
      "  \"variables\": {",
      "    \"var\": \"val\"",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration var = (VariableDeclaration) tree.statements().get(0);

    assertThat(var.declaratedName()).hasKind(ArmTree.Kind.IDENTIFIER).hasValue("var").hasRange(3, 4, 3, 9);
    assertThat(var.value()).asStringLiteral().hasValue("val").hasRange(3, 11, 3, 16);

    assertThat(var.children()).hasSize(2);
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

  @Test
  void shouldParseVariableCaseInsensitive() {
    String code = code("{",
      "  \"VARIABLES\": {",
      "    \"var\": \"val\"",
      "  }",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(VARIABLE_DECLARATION)).isTrue();

    VariableDeclaration var = (VariableDeclaration) tree.statements().get(0);

    assertThat(var.declaratedName()).hasKind(ArmTree.Kind.IDENTIFIER).hasValue("var").hasRange(3, 4, 3, 9);
    assertThat(var.value()).asStringLiteral().hasValue("val").hasRange(3, 11, 3, 16);

    assertThat(var.children()).hasSize(2);
  }
}
