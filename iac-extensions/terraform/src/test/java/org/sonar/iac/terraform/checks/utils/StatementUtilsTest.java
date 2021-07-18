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
import org.sonar.iac.terraform.api.tree.LiteralExprTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.AttributeBuilder.attribute;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;

class StatementUtilsTest {

  private static final LiteralExprTree str = stringExpr("foo");
  private static final AttributeTree attr1 = attribute().key("statement1").value(str).build();
  private static final AttributeTree attr2 = attribute().key("statement2").build();
  private static final BlockTree block1 = block().key("statement1").build();
  private static final BlockTree block2 = block().key("statement3").build();
  private static final BlockTree blockTree = block().statements(Arrays.asList(attr1, attr2, block1, block2)).build();

  @Test
  void test_getAttributeValue() {
    assertThat(StatementUtils.getAttributeValue(blockTree, "statement1")).isPresent().get().isEqualTo(str);
    assertThat(StatementUtils.getAttributeValue(blockTree, "statement3")).isNotPresent();
  }

  @Test
  void test_getBlock() {
    assertThat(StatementUtils.getBlock(blockTree, "statement1")).isPresent().get().isEqualTo(block1);
    assertThat(StatementUtils.getBlock(blockTree, "statement2")).isNotPresent();
  }
}
