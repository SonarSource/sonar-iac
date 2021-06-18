/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ForTupleTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ForTupleTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_for_tuple() {
    ForTupleTree tree = parse("[for a in b : c]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_TUPLE);
    assertThat(tree.condition()).isNotPresent();
    assertThat(tree.loopVariables().trees()).hasSize(1);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.expression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
  }

  @Test
  void with_two_loop_variables() {
    ForTupleTree tree = parse("[for a,b in c : d]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_TUPLE);
    assertThat(tree.condition()).isNotPresent();
    assertThat(tree.loopVariables().trees()).hasSize(2);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopVariables().trees().get(1)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.expression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
  }

  @Test
  void with_condition() {
    ForTupleTree tree = parse("[for a in b : c if true]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_TUPLE);
    assertThat(tree.condition()).isPresent();
    assertThat(tree.condition().get()).isInstanceOfSatisfying(LiteralExprTree.class, a -> assertThat(a.value()).isEqualTo("true"));
    assertThat(tree.loopVariables().trees()).hasSize(1);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.expression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
  }
}
