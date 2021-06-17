/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ParenthesizedExpressionTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ParenthesizedExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_parenthesized_expression() {
    ParenthesizedExpressionTree tree = parse("(a)", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.PARENTHESIZED_EXPRESSION);
    assertThat(tree.children()).hasSize(3);
    assertThat(tree.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
  }
}
