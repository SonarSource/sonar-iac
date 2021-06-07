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
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralExprTreeImplTest extends TerraformTreeModelTest {

  @Test
  void boolean_literal() {
    LiteralExprTree tree = parse("true", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> {
      assertThat(o.getKind()).isEqualTo(TerraformTree.Kind.BOOLEAN_LITERAL);
      assertThat(o.is(TerraformTree.Kind.BOOLEAN_LITERAL)).isTrue();
      assertThat(o.value()).isEqualTo("true");
    });
  }

  @Test
  void null_literal() {
    LiteralExprTree tree = parse("null", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> {
      assertThat(o.getKind()).isEqualTo(TerraformTree.Kind.NULL_LITERAL);
      assertThat(o.value()).isEqualTo("null");
    });
  }

  @Test
  void numeric_literal() {
    LiteralExprTree tree = parse("1", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> {
      assertThat(o.getKind()).isEqualTo(TerraformTree.Kind.NUMERIC_LITERAL);
      assertThat(o.value()).isEqualTo("1");
    });
  }

  @Test
  void heredoc_literal() {
    LiteralExprTree tree = parse("<<EOF\nfoo\nEOF", HclLexicalGrammar.LITERAL_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> {
      assertThat(o.getKind()).isEqualTo(TerraformTree.Kind.HEREDOC_LITERAL);
      assertThat(o.value()).isEqualTo("<<EOF\nfoo\nEOF");
    });
  }
}
