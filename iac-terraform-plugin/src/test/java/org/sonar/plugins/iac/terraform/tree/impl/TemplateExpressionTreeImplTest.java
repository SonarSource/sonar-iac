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
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.VariableExprTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void literal_tree_is_produced_when_no_interpolation_exists() {
    LiteralExprTree tree = parse("\"abc\"", HclLexicalGrammar.TEMPLATE_EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.STRING_LITERAL);
      assertThat(o.value()).isEqualTo("abc");
    });
  }

  @Test
  void simple_quoted_interpolation() {
    TemplateExpressionTree tree = parse("\"ab${x}\"", HclLexicalGrammar.TEMPLATE_EXPRESSION);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.TEMPLATE_EXPRESSION);
      assertThat(o.parts()).hasSize(2);
      assertThat(o.parts().get(0)).isInstanceOfSatisfying(LiteralExprTree.class, p -> {
        assertThat(p.getKind()).isEqualTo(Tree.Kind.TEMPLATE_STRING_PART_LITERAL);
        assertThat(p.value()).isEqualTo("ab");
      });
      assertThat(o.parts().get(1)).isInstanceOfSatisfying(TemplateInterpolationTreeImpl.class, p -> {
        assertThat(p.getKind()).isEqualTo(Tree.Kind.TEMPLATE_INTERPOLATION);
        assertThat(p.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("x"));
      });
    });
  }
}
