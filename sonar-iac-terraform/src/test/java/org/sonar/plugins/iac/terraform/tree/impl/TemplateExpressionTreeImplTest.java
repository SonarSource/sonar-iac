/*
 * SonarQube IaC Plugin
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
import org.sonar.plugins.iac.terraform.api.tree.BinaryExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.TemplateForDirectiveTree;
import org.sonar.plugins.iac.terraform.api.tree.TemplateIfDirectiveTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.VariableExprTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void literal_tree_is_produced_when_no_interpolation_exists() {
    LiteralExprTree tree = parse("\"abc\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.STRING_LITERAL);
      assertThat(o.value()).isEqualTo("abc");
    });
  }

  @Test
  void simple_quoted_interpolation() {
    TemplateExpressionTree tree = parse("\"ab${x}\"", HclLexicalGrammar.QUOTED_TEMPLATE);
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

  @Test
  void simple_quoted_if_directive() {
    TemplateExpressionTree tree = parse("\"%{ if a != 1 }foo%{ else }bar%{ endif }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.TEMPLATE_EXPRESSION);
      assertThat(o.parts()).hasSize(1);
      assertThat(o.parts().get(0)).isInstanceOfSatisfying(TemplateIfDirectiveTree.class, p -> {
        assertThat(p.getKind()).isEqualTo(Tree.Kind.TEMPLATE_DIRECTIVE_IF);
        assertThat(p.condition()).isInstanceOf(BinaryExpressionTree.class);
        assertThat(p.trueExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
        assertThat(p.falseExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("bar"));
      });
    });
  }

  @Test
  void quoted_if_directive_without_else() {
    TemplateExpressionTree tree = parse("\"%{ if a != 1 }foo%{ endif }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.TEMPLATE_EXPRESSION);
      assertThat(o.parts()).hasSize(1);
      assertThat(o.parts().get(0)).isInstanceOfSatisfying(TemplateIfDirectiveTree.class, p -> {
        assertThat(p.getKind()).isEqualTo(Tree.Kind.TEMPLATE_DIRECTIVE_IF);
        assertThat(p.condition()).isInstanceOf(BinaryExpressionTree.class);
        assertThat(p.trueExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
        assertThat(p.falseExpression()).isNull();
      });
    });
  }

  @Test
  void simple_quoted_for_directive() {
    TemplateExpressionTree tree = parse("\"%{ for a in b}foo%{ endfor }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree).satisfies(o -> {
      assertThat(o.getKind()).isEqualTo(Tree.Kind.TEMPLATE_EXPRESSION);
      assertThat(o.parts()).hasSize(1);
      assertThat(o.parts().get(0)).isInstanceOfSatisfying(TemplateForDirectiveTree.class, p -> {
        assertThat(p.getKind()).isEqualTo(Tree.Kind.TEMPLATE_DIRECTIVE_FOR);
        assertThat(p.loopVariables().treesAndSeparators()).hasSize(1);
        assertThat(p.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
        assertThat(p.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("b"));
        assertThat(p.expression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
      });
    });
  }
}
