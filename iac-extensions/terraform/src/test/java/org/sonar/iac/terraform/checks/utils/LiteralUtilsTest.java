/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;


import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;

class LiteralUtilsTest {

  @Test
  void test_isFalse() {
    assertThat(LiteralUtils.isFalse(booleanExpr("FALSE"))).isTrue();
    assertThat(LiteralUtils.isFalse(booleanExpr("TRUE"))).isFalse();
    assertThat(LiteralUtils.isFalse(booleanExpr("false"))).isTrue();
    assertThat(LiteralUtils.isFalse(stringExpr("false"))).isFalse();
    assertThat(LiteralUtils.isFalse(new VariableExprTreeImpl(null))).isFalse();
  }

  @Test
  void test_isValue() {
    assertThat(LiteralUtils.isValue(booleanExpr("FALSE"), "FALSE")).isTrue();
    assertThat(LiteralUtils.isValue(booleanExpr("FALSE"), "false")).isFalse();
    assertThat(LiteralUtils.isValue(stringExpr("\"false\""), "false")).isTrue();
    assertThat(LiteralUtils.isValue(stringExpr("false"), "false")).isFalse();
    assertThat(LiteralUtils.isValue(new VariableExprTreeImpl(null), "false")).isFalse();
  }

}
