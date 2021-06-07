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
import org.sonar.iac.terraform.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class IndexAccessExprTreeImplTest extends TerraformTreeModelTest {
  @Test
  void simple_index_access() {
    IndexAccessExprTree tree = parse("a[1]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(a -> {
      assertThat(a.getKind()).isEqualTo(Tree.Kind.INDEX_ACCESS_EXPR);
      assertThat(a.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
      assertThat(a.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("1"));
      assertThat(a.children()).hasSize(4);
    });
  }

  @Test
  void double_index_access() {
    IndexAccessExprTree tree = parse("a[1][2]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(a -> {
      assertThat(a.subject()).isInstanceOfSatisfying(IndexAccessExprTreeImpl.class, o -> {
        assertThat(o.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
        assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
        assertThat(o.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, ob -> assertThat(ob.value()).isEqualTo("1"));
      });
      assertThat(a.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("2"));
    });
  }

  @Test
  void attribute_access_subject() {
    IndexAccessExprTree tree = parse("a.b[1]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(a -> {
      assertThat(a.subject()).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, o -> {
        assertThat(o.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
        assertThat(o.attribute().value()).isEqualTo("b");
      });
      assertThat(a.index()).isInstanceOfSatisfying(LiteralExprTreeImpl.class, o -> assertThat(o.value()).isEqualTo("1"));
    });
  }
}
