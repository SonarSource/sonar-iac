package org.sonar.plugins.iac.terraform.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.iac.terraform.api.tree.BlockTree;
import org.sonar.plugins.iac.terraform.parser.HclLexicalGrammar;

import static org.assertj.core.api.Assertions.assertThat;

class BlockTreeImplTest extends TerraformTreeModelTest {

  @Test
  void simple_one_line_block() {
    BlockTree tree = parse("a{\n b = true \nc = null}", HclLexicalGrammar.BLOCK);
    assertThat(tree).isInstanceOfSatisfying(BlockTree.class, o -> {
      assertThat(o.type().text()).isEqualTo("a");
      assertThat(o.labels()).isEmpty();
      assertThat(o.body().statements()).hasSize(2);
    });
  }
}
