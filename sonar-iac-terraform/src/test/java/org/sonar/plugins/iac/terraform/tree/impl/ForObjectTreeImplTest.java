/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.ForObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.VariableExprTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ForObjectTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_for_tuple() {
    ForObjectTree tree = parse("{for a in b : c => d}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.FOR_OBJECT);
      assertThat(o.condition()).isNotPresent();
      assertThat(o.hasEllipsis()).isFalse();
      assertThat(o.loopVariables().trees()).hasSize(1);
      assertThat(o.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
      assertThat(o.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
      assertThat(o.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
      assertThat(o.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
    });
  }

  @Test
  void with_two_loop_variables() {
    ForObjectTree tree = parse("{for a,b in c : d => e}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.FOR_OBJECT);
      assertThat(o.condition()).isNotPresent();
      assertThat(o.hasEllipsis()).isFalse();
      assertThat(o.loopVariables().trees()).hasSize(2);
      assertThat(o.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
      assertThat(o.loopVariables().trees().get(1)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
      assertThat(o.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
      assertThat(o.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
      assertThat(o.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("e"));
    });
  }

  @Test
  void with_condition() {
    ForObjectTree tree = parse("{for a in b : c => d if true}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.FOR_OBJECT);
      assertThat(o.condition()).isPresent();
      assertThat(o.condition().get()).isInstanceOfSatisfying(LiteralExprTree.class, a -> assertThat(a.value()).isEqualTo("true"));
      assertThat(o.hasEllipsis()).isFalse();
      assertThat(o.loopVariables().trees()).hasSize(1);
      assertThat(o.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
      assertThat(o.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
      assertThat(o.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
      assertThat(o.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
    });
  }

  @Test
  void with_ellipsis() {
    ForObjectTree tree = parse("{for a in b : c => d...}", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.FOR_OBJECT);
      assertThat(o.condition()).isNotPresent();
      assertThat(o.hasEllipsis()).isTrue();
      assertThat(o.loopVariables().trees()).hasSize(1);
      assertThat(o.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("a"));
      assertThat(o.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("b"));
      assertThat(o.firstExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("c"));
      assertThat(o.secondExpression()).isInstanceOfSatisfying(VariableExprTree.class, a -> assertThat(a.name()).isEqualTo("d"));
    });
  }
}
