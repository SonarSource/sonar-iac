/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.AttributeBuilder.attribute;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;

class StatementUtilsTest {

  private static final AttributeTree attr1 = attribute().name("attr1").build();
  private static final AttributeTree attr2 = attribute().name("attr2").build();
  private static final BlockTree subBlock1 = block().type("subblock1").build();
  private static final BlockTree subBlock2 = block().type("subblock2").build();
  private static final BlockTree blockTree = block().statements(Arrays.asList(attr1, attr2, subBlock1, subBlock2)).build();

  @Test
  void test_hasAttribute() {
    assertThat(StatementUtils.hasAttribute(blockTree, "attr1")).isTrue();
    assertThat(StatementUtils.hasAttribute(blockTree, "attr3")).isFalse();
    assertThat(StatementUtils.hasAttribute(blockTree, "subblock1")).isFalse();
  }

  @Test
  void test_hasBlock() {
    assertThat(StatementUtils.hasBlock(blockTree, "subblock1")).isTrue();
    assertThat(StatementUtils.hasBlock(blockTree, "subblock3")).isFalse();
    assertThat(StatementUtils.hasBlock(blockTree, "attr1")).isFalse();
  }

  @Test
  void test_getAttribute() {
    assertThat(StatementUtils.getAttribute(blockTree, "attr1")).isPresent().get().isEqualTo(attr1);
    assertThat(StatementUtils.getAttribute(blockTree, "attr3")).isNotPresent();
    assertThat(StatementUtils.getAttribute(blockTree, "subblock1")).isNotPresent();
  }

  @Test
  void test_getBlock() {
    assertThat(StatementUtils.getBlock(blockTree, "subblock1")).isPresent().get().isEqualTo(subBlock1);
    assertThat(StatementUtils.getBlock(blockTree, "subblock3")).isNotPresent();
    assertThat(StatementUtils.getBlock(blockTree, "attr1")).isNotPresent();
  }
}
