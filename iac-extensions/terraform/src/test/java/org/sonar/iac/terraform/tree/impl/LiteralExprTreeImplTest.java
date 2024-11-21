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
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralExprTreeImplTest extends TerraformTreeModelTest {

  @Test
  void boolean_literal() {
    LiteralExprTree tree = parse("true", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BOOLEAN_LITERAL);
    assertThat(tree.is(TerraformTree.Kind.BOOLEAN_LITERAL)).isTrue();
    assertThat(tree.value()).isEqualTo("true");
  }

  @Test
  void null_literal() {
    LiteralExprTree tree = parse("null", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.NULL_LITERAL);
    assertThat(tree.value()).isEqualTo("null");
  }

  @Test
  void numeric_literal() {
    LiteralExprTree tree = parse("1", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.NUMERIC_LITERAL);
    assertThat(tree.value()).isEqualTo("1");
  }

  @Test
  void heredoc_literal() {
    LiteralExprTree tree = parse("<<EOF\nfoo\nEOF", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.HEREDOC_LITERAL);
    assertThat(tree.value()).isEqualTo("<<EOF\nfoo\nEOF");
  }
}
