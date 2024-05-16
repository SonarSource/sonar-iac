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
package org.sonar.iac.terraform.parser;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.TextRangeAssert;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;
import org.sonar.iac.terraform.parser.utils.Assertions;

class BlockTest {

  @Test
  void testBlock() {
    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .matches("a{\n b = true \nc = null}")
      .matches("a {\n}")
      .matches("a \"label\" {\n}")
      .matches("  a {\n   }")
      .matches("a { /* a */  /* b */\n}")
      .matches("""
        dynamic "a" "label" {
        }""")
      .matches("""
        dynamic a {
        }""")
      .matches("""
        dynamic a "label" {
        }""")
      .notMatches("a{}")
      .notMatches("a")
      .notMatches("");
  }

  @Test
  void testOneLineBlock() {
    Assertions.assertThat(HclLexicalGrammar.ONE_LINE_BLOCK)
      .matches("a{}")
      .matches("  a {   }")
      .matches("a { \n }")
      .matches("a label {}")
      .matches("a \"label\" {}")
      .matches("a \"label1\" \"label2\" {}")
      .matches("a \"label with \\\" quote\" {}")
      .matches("a \"label1\" label2 {}")
      .matches("dynamic a {}")
      .matches("dynamic a \"label1\" label2 {}")
      .matches("a {b = false}")
      .notMatches("a")
      .notMatches("a{");
  }

  @Test
  void testDynamicBlock() {
    var code = """
      dynamic "a" {
        for_each = var.a
        content {
          b = a.value.b
        }
      }
      """;

    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .extracting(parser -> parser.parse(code))
      .isNotNull()
      .satisfies(tree -> {
        var blockTree = (BlockTree) tree;
        org.assertj.core.api.Assertions.assertThat(blockTree.isDynamic()).isTrue();
        org.assertj.core.api.Assertions.assertThat(blockTree.key().value()).isEqualTo("a");
        TextRangeAssert.assertThat(blockTree.key().textRange()).hasRange(1, 9, 1, 10);
      });
  }
}
