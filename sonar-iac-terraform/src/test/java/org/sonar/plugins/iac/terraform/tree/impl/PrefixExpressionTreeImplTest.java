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
import org.sonar.plugins.iac.terraform.api.tree.PrefixExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.VariableExprTree;
import org.sonar.plugins.iac.terraform.api.tree.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void single_prefix() {
    PrefixExpressionTree tree = parse("!a", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.PREFIX_EXPRESSION);
      assertThat(o.children()).hasSize(2);
      assertThat(o.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("!"));
      assertThat(o.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
    });
  }

  @Test
  void multiple_prefixes_order() {
    PrefixExpressionTree tree = parse("-!a", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("-"));
      assertThat(o.expression()).isInstanceOfSatisfying(PrefixExpressionTree.class, p -> {
        assertThat(p.prefix()).isInstanceOfSatisfying(SyntaxToken.class, v -> assertThat(v.value()).isEqualTo("!"));
        assertThat(p.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
      });
    });
  }
}
