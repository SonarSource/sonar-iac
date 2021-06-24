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

class BlockTreeImplTest extends TerraformTreeModelTest {

  @Test
  void empty_block() {
    BlockTree tree = parse("a{}", HclLexicalGrammar.BLOCK);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BLOCK);
    assertThat(tree.type().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).isEmpty();
  }

  @Test
  void simple_one_line_block() {
    BlockTree tree = parse("a{\n b = true \nc = null}", HclLexicalGrammar.BLOCK);
    assertThat(tree.getKind()).isEqualTo(TerraformTree.Kind.BLOCK);
    assertThat(tree.type().value()).isEqualTo("a");
    assertThat(tree.labels()).isEmpty();
    assertThat(tree.statements()).hasSize(2);
  }
}
