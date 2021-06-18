/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.PrefixExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void single_prefix() {
    PrefixExpressionTree tree = parse("!a", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.PREFIX_EXPRESSION);
    assertThat(tree.children()).hasSize(2);
    assertThat(tree.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("!"));
    assertThat(tree.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
  }

  @Test
  void multiple_prefixes_order() {
    PrefixExpressionTree tree = parse("-!a", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("-"));
    assertThat(tree.expression()).isInstanceOfSatisfying(PrefixExpressionTree.class, p -> {
      assertThat(p.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("!"));
      assertThat(p.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
    });
  }
}
