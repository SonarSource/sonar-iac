/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.BinaryExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class BinaryExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_binary_expression() {
    BinaryExpressionTree tree = parse("a + b", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BINARY_EXPRESSION);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.leftOperand()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
    assertThat(tree.rightOperand()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("b"));
    assertThat(tree.operator()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("+"));
  }
}
