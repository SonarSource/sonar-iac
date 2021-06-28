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

  private static final AttributeTree attr1 = attribute().identifier("statement1").build();
  private static final AttributeTree attr2 = attribute().identifier("statement2").build();
  private static final BlockTree block1 = block().identifier("statement1").build();
  private static final BlockTree block2 = block().identifier("statement3").build();
  private static final BlockTree blockTree = block().statements(Arrays.asList(attr1, attr2, block1, block2)).build();

  @Test
  void test_hasStatement() {
    assertThat(StatementUtils.hasStatement(blockTree, "statement1")).isTrue();
    assertThat(StatementUtils.hasStatement(blockTree, "statement2")).isTrue();
    assertThat(StatementUtils.hasStatement(blockTree, "statement4")).isFalse();
  }

  @Test
  void test_hasAttribute() {
    assertThat(StatementUtils.hasAttribute(blockTree, "statement1")).isTrue();
    assertThat(StatementUtils.hasAttribute(blockTree, "statement2")).isTrue();
    assertThat(StatementUtils.hasAttribute(blockTree, "statement3")).isFalse();
  }

  @Test
  void test_hasBlock() {
    assertThat(StatementUtils.hasBlock(blockTree, "statement1")).isTrue();
    assertThat(StatementUtils.hasBlock(blockTree, "statement2")).isFalse();
    assertThat(StatementUtils.hasBlock(blockTree, "statement3")).isTrue();
  }

  @Test
  void test_getAttribute() {
    assertThat(StatementUtils.getAttribute(blockTree, "statement1")).isPresent().get().isEqualTo(attr1);
    assertThat(StatementUtils.getAttribute(blockTree, "statement3")).isNotPresent();
  }

  @Test
  void test_getBlock() {
    assertThat(StatementUtils.getBlock(blockTree, "statement1")).isPresent().get().isEqualTo(block1);
    assertThat(StatementUtils.getBlock(blockTree, "statement2")).isNotPresent();
  }
}
