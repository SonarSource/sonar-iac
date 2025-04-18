/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.BinaryExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TemplateExpressionTree;
import org.sonar.iac.terraform.api.tree.TemplateForDirectiveTree;
import org.sonar.iac.terraform.api.tree.TemplateIfDirectiveTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateExpressionTreeImplTest extends TerraformTreeModelTest {

  @Test
  void literal_tree_is_produced_when_no_interpolation_exists() {
    LiteralExprTree tree = parse("\"abc\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.STRING_LITERAL);
    assertThat(tree.value()).isEqualTo("abc");
  }

  @Test
  void simple_quoted_interpolation() {
    TemplateExpressionTree tree = parse("\"ab${x}\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_EXPRESSION);
    assertThat(tree.parts()).hasSize(2);
    assertThat(tree.parts().get(0)).isInstanceOfSatisfying(LiteralExprTree.class, p -> {
      assertThat(p.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_STRING_PART_LITERAL);
      assertThat(p.value()).isEqualTo("ab");
    });
    assertThat(tree.parts().get(1)).isInstanceOfSatisfying(TemplateInterpolationTreeImpl.class, p -> {
      assertThat(p.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_INTERPOLATION);
      assertThat(p.expression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("x"));
    });
  }

  @Test
  void simple_quoted_if_directive() {
    TemplateExpressionTree tree = parse("\"%{ if a != 1 }foo%{ else }bar%{ endif }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_EXPRESSION);
    assertThat(tree.parts()).hasSize(1);
    assertThat(tree.parts().get(0)).isInstanceOfSatisfying(TemplateIfDirectiveTree.class, p -> {
      assertThat(p.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_DIRECTIVE_IF);
      assertThat(p.condition()).isInstanceOf(BinaryExpressionTree.class);
      assertThat(p.trueExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
      assertThat(p.falseExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("bar"));
    });
  }

  @Test
  void quoted_if_directive_without_else() {
    TemplateExpressionTree tree = parse("\"%{ if a != 1 }foo%{ endif }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_EXPRESSION);
    assertThat(tree.parts()).hasSize(1);
    assertThat(tree.parts().get(0)).isInstanceOfSatisfying(TemplateIfDirectiveTree.class, p -> {
      assertThat(p.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_DIRECTIVE_IF);
      assertThat(p.condition()).isInstanceOf(BinaryExpressionTree.class);
      assertThat(p.trueExpression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
      assertThat(p.falseExpression()).isNull();
    });
  }

  @Test
  void simple_quoted_for_directive() {
    TemplateExpressionTree tree = parse("\"%{ for a in b}foo%{ endfor }\"", HclLexicalGrammar.QUOTED_TEMPLATE);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_EXPRESSION);
    assertThat(tree.parts()).hasSize(1);
    assertThat(tree.parts().get(0)).isInstanceOfSatisfying(TemplateForDirectiveTree.class, p -> {
      assertThat(p.getKind()).isEqualTo(TerraformTree.Kind.TEMPLATE_DIRECTIVE_FOR);
      assertThat(p.loopVariables().treesAndSeparators()).hasSize(1);
      assertThat(p.loopVariables().trees().get(0)).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
      assertThat(p.loopExpression()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("b"));
      assertThat(p.expression()).isInstanceOfSatisfying(LiteralExprTree.class, l -> assertThat(l.value()).isEqualTo("foo"));
    });
  }
}
