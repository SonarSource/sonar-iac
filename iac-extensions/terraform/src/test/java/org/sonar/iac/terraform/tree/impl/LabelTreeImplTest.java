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
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class LabelTreeImplTest extends TerraformTreeModelTest {

  @Test
  void string_literal() {
    LabelTree tree = parse("\"a\"", HclLexicalGrammar.LABEL);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.LABEL);
    assertThat(tree.value()).isEqualTo("a");
    assertThat(tree.value()).isNotEqualTo(tree.token().value());
  }

  @Test
  void identifier() {
    LabelTree tree = parse("id", HclLexicalGrammar.LABEL);
    assertThat(tree.value()).isEqualTo("id");
    assertThat(tree.value()).isEqualTo(tree.token().value());
  }
}
