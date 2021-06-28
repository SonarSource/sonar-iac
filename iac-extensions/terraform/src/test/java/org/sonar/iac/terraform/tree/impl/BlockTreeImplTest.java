/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.grammar.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class BlockTreeImplTest extends TerraformTreeModelTest {

  @Test
  void empty_block() {
    BlockTree tree = parse("a{}", HclLexicalGrammar.BLOCK);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void simple_block() {
    BlockTree tree = parse("a{\n b = true \nc = null}", HclLexicalGrammar.BLOCK);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).hasSize(2);
  }

  @Test
  void simple_one_line_block() {
    BlockTree tree = parse("a {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).isEmpty();
    assertTextRange(tree.textRange()).hasRange(1,0,1,4);
  }

  @Test
  void with_string_label() {
    BlockTree tree = parse("a \"label\" {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).hasSize(1);
    assertThat(tree.labels().get(0).value()).isEqualTo("\"label\"");
  }

  @Test
  void with_multiple_strings_labels() {
    BlockTree tree = parse("a \"label1\" \"label2\" {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).hasSize(2);
    assertThat(tree.labels().get(0).value()).isEqualTo("\"label1\"");
    assertThat(tree.labels().get(1).value()).isEqualTo("\"label2\"");
  }

  @Test
  void with_string_and_identifier_labels() {
    BlockTree tree = parse("a \"label1\" label2 {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).hasSize(2);
    assertThat(tree.labels().get(0).value()).isEqualTo("\"label1\"");
    assertThat(tree.labels().get(1).value()).isEqualTo("label2");
  }

  @Test
  void with_simple_attribute() {
    BlockTree tree = parse("a { b = true }", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0)).isInstanceOf(AttributeTreeImpl.class);
  }

  @Test
  void with_singleline_comment1() {
    BlockTree tree = parse("#comment\na {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.identifier().comments()).hasSize(1);
    assertThat(tree.identifier().comments().get(0)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("#comment");
      assertTextRange(t.textRange()).hasRange(1,0,1,8);
    });
  }

  @Test
  void with_singleline_comment2() {
    BlockTree tree = parse("//comment\na {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.identifier().comments()).hasSize(1);
    assertThat(tree.identifier().comments().get(0)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("//comment");
      assertTextRange(t.textRange()).hasRange(1,0,1,9);
    });
  }

  @Test
  void with_multiple_singleline_comment() {
    BlockTree tree = parse("#comment1\n#comment2\na {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.identifier().comments()).hasSize(2);
    assertThat(tree.identifier().comments().get(0)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("#comment1");
      assertTextRange(t.textRange()).hasRange(1,0,1,9);
    });
    assertThat(tree.identifier().comments().get(1)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("#comment2");
      assertTextRange(t.textRange()).hasRange(2,0,2,9);
    });
  }

  @Test
  void with_multiline_comment() {
    BlockTree tree = parse("/* line1\nline2 */\na {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.identifier().comments()).hasSize(1);
    assertThat(tree.identifier().comments().get(0)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("/* line1\nline2 */");
      assertTextRange(t.textRange()).hasRange(1,0,2,8);
    });
  }

  @Test
  void with_multiline_comment_same_line() {
    BlockTree tree = parse("/* comment */a {}", HclLexicalGrammar.ONE_LINE_BLOCK);
    assertThat(tree.identifier().value()).isEqualTo("a");
    assertThat(tree.identifier().comments()).hasSize(1);
    assertThat(tree.identifier().comments().get(0)).satisfies(t -> {
      assertThat(t.value()).isEqualTo("/* comment */");
      assertTextRange(t.textRange()).hasRange(1,0,1,13);
    });
  }
}
