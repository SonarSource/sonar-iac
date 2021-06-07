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
import org.sonar.plugins.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class IndexSplatAccessTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_index_splat_access() {
    IndexSplatAccessTree tree = parse("a[*]", HclLexicalGrammar.EXPRESSION);
    assertThat(tree).satisfies(a -> {
      assertThat(a.getKind()).isEqualTo(Tree.Kind.INDEX_SPLAT_ACCESS);
      assertThat(a.children()).hasSize(4);
      assertThat(a.subject()).isInstanceOfSatisfying(VariableExprTreeImpl.class, o -> assertThat(o.name()).isEqualTo("a"));
    });
  }
}
