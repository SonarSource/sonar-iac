/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
