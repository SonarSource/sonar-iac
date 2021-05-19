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
import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_object() {
    ObjectTree tree = parse("{a: 1, b: 2}", HclLexicalGrammar.OBJECT);
    assertThat(tree).isInstanceOfSatisfying(ObjectTreeImpl.class, o -> assertThat(o.elements()).hasSize(2));
  }

  @Test
  void newline_separated_elements() {
    ObjectTree tree = parse("{a: 1\n b: 2}", HclLexicalGrammar.OBJECT);
    assertThat(tree).isInstanceOfSatisfying(ObjectTreeImpl.class, o -> assertThat(o.elements()).hasSize(2));
  }
}
