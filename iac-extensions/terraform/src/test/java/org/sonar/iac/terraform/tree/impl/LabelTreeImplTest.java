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
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LabelTreeImplTest extends TerraformTreeModelTest {

  @Test
  void string_literal() {
    LabelTree tree = parse("\"a\"", HclLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.LABEL);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.value()).isNotEqualTo(tree.token().value());
  }

  @Test
  void identifier() {
    LabelTree tree = parse("id", HclLexicalGrammar.LABEL);
    assertThat(tree.value()).isEqualTo("id");
    assertThat(tree.value()).isEqualTo(tree.token().value());
  }
}
