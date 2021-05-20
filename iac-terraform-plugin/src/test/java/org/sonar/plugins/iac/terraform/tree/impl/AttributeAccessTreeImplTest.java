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
import org.sonar.plugins.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_attribute_access() {
    AttributeAccessTree tree = parse("a.b", HclLexicalGrammar.ATTRIBUTE_ACCESS_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, a -> {
      assertThat(a.attribute().value()).isEqualTo("b");
      assertThat(a.accessToken()).isInstanceOfSatisfying(SyntaxToken.class, s -> assertThat(s.value()).isEqualTo("."));
      assertThat(a.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
    });
  }

  @Test
  void double_attribute_access() {
    AttributeAccessTree tree = parse("a.b.c", HclLexicalGrammar.ATTRIBUTE_ACCESS_EXPRESSION);
    assertThat(tree).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, a -> {
      assertThat(a.attribute().value()).isEqualTo("c");
      assertThat(a.object()).isInstanceOfSatisfying(AttributeAccessTreeImpl.class, o -> {
        assertThat(o.attribute().value()).isEqualTo("b");
        assertThat(o.object()).isInstanceOfSatisfying(VariableExprTreeImpl.class, ob -> assertThat(ob.name()).isEqualTo("a"));
      });
    });
  }
}
