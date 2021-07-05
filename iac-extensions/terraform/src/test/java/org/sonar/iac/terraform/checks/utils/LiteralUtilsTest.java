/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;


import org.junit.jupiter.api.Test;
import org.sonar.iac.common.checks.Trilean;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;

class LiteralUtilsTest {

  @Test
  void test_isFalse() {
    assertThat(LiteralUtils.isBooleanFalse(booleanExpr("FALSE"))).isEqualTo(Trilean.TRUE);
    assertThat(LiteralUtils.isBooleanFalse(booleanExpr("TRUE"))).isEqualTo(Trilean.FALSE);
    assertThat(LiteralUtils.isBooleanFalse(booleanExpr("false"))).isEqualTo(Trilean.TRUE);
    assertThat(LiteralUtils.isBooleanFalse(stringExpr("false"))).isEqualTo(Trilean.UNKNOWN);
    assertThat(LiteralUtils.isBooleanFalse(new VariableExprTreeImpl(null))).isEqualTo(Trilean.UNKNOWN);
  }

  @Test
  void test_isValue() {
    assertThat(LiteralUtils.isValue(booleanExpr("FALSE"), "FALSE")).isEqualTo(Trilean.TRUE);
    assertThat(LiteralUtils.isValue(booleanExpr("FALSE"), "false")).isEqualTo(Trilean.FALSE);
    assertThat(LiteralUtils.isValue(stringExpr("\"false\""), "false")).isEqualTo(Trilean.TRUE);
    assertThat(LiteralUtils.isValue(stringExpr("false"), "false")).isEqualTo(Trilean.FALSE);
    assertThat(LiteralUtils.isValue(new VariableExprTreeImpl(null), "false")).isEqualTo(Trilean.UNKNOWN);
  }

}
