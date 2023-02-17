/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.ForObjectTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ForObjectTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_for_tuple() {
    ForObjectTree tree = parse("{for a in b : c => d}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_OBJECT);
    assertThat(tree.condition()).isNotPresent();
    assertThat(tree.hasEllipsis()).isFalse();
    assertThat(tree.loopVariables().trees()).hasSize(1);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
  }

  @Test
  void with_two_loop_variables() {
    ForObjectTree tree = parse("{for a,b in c : d => e}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_OBJECT);
    assertThat(tree.condition()).isNotPresent();
    assertThat(tree.hasEllipsis()).isFalse();
    assertThat(tree.loopVariables().trees()).hasSize(2);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopVariables().trees().get(1)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
    assertThat(tree.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("e"));
  }

  @Test
  void with_condition() {
    ForObjectTree tree = parse("{for a in b : c => d if true}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_OBJECT);
    assertThat(tree.condition()).isPresent();
    assertThat(tree.condition().get()).isInstanceOfSatisfying(LiteralExprTree.class, a -> assertThat(a.value()).isEqualTo("true"));
    assertThat(tree.hasEllipsis()).isFalse();
    assertThat(tree.loopVariables().trees()).hasSize(1);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
  }

  @Test
  void with_ellipsis() {
    ForObjectTree tree = parse("{for a in b : c => d...}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.FOR_OBJECT);
    assertThat(tree.condition()).isNotPresent();
    assertThat(tree.hasEllipsis()).isTrue();
    assertThat(tree.loopVariables().trees()).hasSize(1);
    assertThat(tree.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
    assertThat(tree.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
    assertThat(tree.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
    assertThat(tree.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
  }
}
