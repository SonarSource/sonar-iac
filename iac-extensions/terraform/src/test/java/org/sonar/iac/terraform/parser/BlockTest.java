/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
      .matches("a{\n b = true \nc = NULL}")
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
      .matches("""
        dynamic "a" label {
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
  void testBlockDetailed() {
    var code = """
      a "label" {
        b = value.b
      }
      """;

    Assertions.assertThat(HclLexicalGrammar.BLOCK)
      .extracting(parser -> parser.parse(code))
      .isNotNull()
      .satisfies(tree -> {
        var blockTree = (BlockTree) tree;
        org.assertj.core.api.Assertions.assertThat(blockTree.isDynamic()).isFalse();
        org.assertj.core.api.Assertions.assertThat(blockTree.key().value()).isEqualTo("a");
        TextRangeAssert.assertThat(blockTree.key().textRange()).hasRange(1, 0, 1, 1);
      });
  }

  @Test
  void testDynamicBlockDetailed() {
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
