/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ConditionTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_condition() {
    ConditionTree tree = parse("a ? b : c", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.children()).hasSize(5);
    assertCondition(tree, "a", "b", "c");
  }

  @Test
  void nested_condition() {
    ConditionTree tree = parse("a ? a1 : a2 ? b ? b1 : b2 : c ? c1 : c2", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.conditionExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
    assertThat(tree.trueExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a1"));
    assertThat(tree.falseExpression()).isInstanceOfSatisfying(ConditionTree.class, o -> {
      assertThat(o.conditionExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a2"));
      assertCondition(o.trueExpression(), "b", "b1", "b2");
      assertCondition(o.falseExpression(), "c", "c1", "c2");
    });
  }

  @Test
  void nested_with_simple_condition_expression() {
    ConditionTree tree = parse("a ? b ? b1 : b2 : c ? c1 : c2", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.conditionExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
    assertCondition(tree.trueExpression(), "b", "b1", "b2");
    assertCondition(tree.falseExpression(), "c", "c1", "c2");
  }

  private void assertCondition(ExpressionTree tree, String condVarName, String trueVarName, String falseVarName) {
    assertThat(tree).isInstanceOfSatisfying(ConditionTree.class, o -> {
      assertThat(o.getKind()).isEqualTo(TerraformTree.Kind.CONDITION);
      assertThat(o.conditionExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo(condVarName));
      assertThat(o.trueExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo(trueVarName));
      assertThat(o.falseExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo(falseVarName));
    });
  }
}
