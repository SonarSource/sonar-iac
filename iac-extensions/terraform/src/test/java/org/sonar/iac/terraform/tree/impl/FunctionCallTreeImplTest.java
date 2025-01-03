/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionCallTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_call() {
    FunctionCallTree tree = parse("a(1,2)", HclLexicalGrammar.FUNCTION_CALL);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FUNCTION_CALL);
    assertThat(tree.name().value()).isEqualTo("a");
    assertThat(tree.arguments().trees()).hasSize(2);
    assertThat(tree.arguments().trees().get(0)).isInstanceOfSatisfying(LiteralExprTreeImpl.class, a -> assertThat(a.value()).isEqualTo("1"));
    assertThat(tree.arguments().trees().get(1)).isInstanceOfSatisfying(LiteralExprTreeImpl.class, a -> assertThat(a.value()).isEqualTo("2"));
    assertThat(tree.arguments().separators()).hasSize(1);
  }

  @Test
  void trailing_comma() {
    FunctionCallTree tree = parse("a(1,)", HclLexicalGrammar.FUNCTION_CALL);
    assertThat(tree.name().value()).isEqualTo("a");
    assertThat(tree.arguments().trees()).hasSize(1);
    assertThat(tree.arguments().trees().get(0)).isInstanceOfSatisfying(LiteralExprTreeImpl.class, a -> assertThat(a.value()).isEqualTo("1"));
    assertThat(tree.arguments().separators()).hasSize(1);
  }

  @Test
  void trailing_ellipsis_operator() {
    FunctionCallTree tree = parse("a(b,c...)", HclLexicalGrammar.FUNCTION_CALL);
    assertThat(tree.name().value()).isEqualTo("a");
    assertThat(tree.arguments().trees()).hasSize(2);
    assertThat(tree.arguments().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.arguments().trees().get(1)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.arguments().separators()).hasSize(2);
    assertThat(tree.arguments().separators().get(0)).isInstanceOfSatisfying(SyntaxToken.class, a -> assertThat(a.value()).isEqualTo(","));
    assertThat(tree.arguments().separators().get(1)).isInstanceOfSatisfying(SyntaxToken.class, a -> assertThat(a.value()).isEqualTo("..."));
  }
}
