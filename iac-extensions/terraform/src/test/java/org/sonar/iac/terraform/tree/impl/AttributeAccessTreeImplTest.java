/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_attribute_access() {
    AttributeAccessTree tree = parse("a.b", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ATTRIBUTE_ACCESS);
    assertThat(tree.attribute().value()).isEqualTo("b");
    assertThat(tree.accessToken()).isInstanceOfSatisfying(SyntaxToken.class, s -> assertThat(s.value()).isEqualTo("."));
    assertThat(tree.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
  }

  @Test
  void double_attribute_access() {
    AttributeAccessTreeImpl tree = parse("a.b.c", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("c");
    assertThat(tree.object()).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, o -> {
      assertThat(o.attribute().value()).isEqualTo("b");
      assertThat(o.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
    });
  }

  @Test
  void index_access_object() {
    AttributeAccessTree tree = parse("a[1].c", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("c");
    assertThat(tree.object()).isInstanceOfSatisfying(IndexAccessExprTreeImpl.class, o -> {
      assertThat(o.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
      assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
    });
  }

  @Test
  void legacy_index_access() {
    AttributeAccessTree tree = parse("a.0", HclLexicalGrammar.EXPRESSION);
    assertThat(tree.attribute().value()).isEqualTo("0");
    assertThat(tree.object()).isInstanceOfSatisfying(VariableExprTree.class, v -> assertThat(v.name()).isEqualTo("a"));
  }
}
