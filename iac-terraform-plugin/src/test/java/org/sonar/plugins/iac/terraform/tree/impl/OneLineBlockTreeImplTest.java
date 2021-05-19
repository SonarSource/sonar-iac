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
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class OneLineBlockTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_one_line_block() {
    OneLineBlockTree tree = parse("a {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree).isInstanceOfSatisfying(OneLineBlockTreeImpl.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).isEmpty();
      assertThat(o.attribute()).isNotPresent();
    });
  }

  @Test
  void with_string_label() {
    OneLineBlockTree tree = parse("a \"label\" {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree).isInstanceOfSatisfying(OneLineBlockTreeImpl.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).hasSize(1);
      assertThat(o.labels().get(0).value()).isEqualTo("\"label\"");
    });
  }

  @Test
  void with_multiple_strings_labels() {
    OneLineBlockTree tree = parse("a \"label1\" \"label2\" {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree).isInstanceOfSatisfying(OneLineBlockTreeImpl.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).hasSize(2);
      assertThat(o.labels().get(0).value()).isEqualTo("\"label1\"");
      assertThat(o.labels().get(1).value()).isEqualTo("\"label2\"");
    });
  }

  @Test
  void with_string_and_identifier_labels() {
    OneLineBlockTree tree = parse("a \"label1\" label2 {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree).isInstanceOfSatisfying(OneLineBlockTreeImpl.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).hasSize(2);
      assertThat(o.labels().get(0).value()).isEqualTo("\"label1\"");
      assertThat(o.labels().get(1).value()).isEqualTo("label2");
    });
  }

  @Test
  void with_simple_attribute() {
    OneLineBlockTree tree = parse("a { b = true }", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree).isInstanceOfSatisfying(OneLineBlockTreeImpl.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).isEmpty();
      assertThat(o.attribute().get()).isInstanceOf(AttributeTreeImpl.class);
    });
  }
}
