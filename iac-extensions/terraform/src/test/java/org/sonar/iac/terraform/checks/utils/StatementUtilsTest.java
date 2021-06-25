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
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.AttributeBuilder.attribute;
import static org.sonar.iac.terraform.TestTreeBuilders.BlockBuilder.block;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;

class StatementUtilsTest {

  private static final AttributeTree attr1 = attribute().name("attr1").build();
  private static final AttributeTree attr2 = attribute().name("attr2").build();
  private static final LiteralExprTree literal = stringExpr("string");
  private static final BlockTree blockTree = block().statements(Arrays.asList(attr1, attr2, literal)).build();

  @Test
  void test_hasStatement() {
//    assertThat(StatementUtils.hasStatement(blockTree, "attr1", Kind.ATTRIBUTE)).isTrue();
//    assertThat(StatementUtils.hasStatement(blockTree, "attr3", Kind.ATTRIBUTE)).isFalse();
//    assertThat(StatementUtils.hasStatement(blockTree, "attr1", Kind.BLOCK)).isFalse();
//    assertThat(StatementUtils.hasStatement(blockTree, "attr1", Kind.BOOLEAN_LITERAL)).isFalse();
    assertThat(StatementUtils.hasStatement(blockTree, "string", Kind.STRING_LITERAL)).isFalse();
  }

  @Test
  void test_getStatement() {
    assertThat(StatementUtils.getStatement(blockTree, "attr1", Kind.ATTRIBUTE)).isPresent().get().isEqualTo(attr1);
    assertThat(StatementUtils.getStatement(blockTree, "attr3", Kind.ATTRIBUTE)).isNotPresent();
    assertThat(StatementUtils.getStatement(blockTree, "attr1", Kind.BLOCK)).isNotPresent();
  }
}
