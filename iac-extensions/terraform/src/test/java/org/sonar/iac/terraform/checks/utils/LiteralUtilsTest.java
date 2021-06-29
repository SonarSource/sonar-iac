/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;


import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.AttributeBuilder.attribute;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;

class LiteralUtilsTest {

  @Test
  void test_isFalse() {
    assertThat(AttributeUtils.isFalse(attr(booleanExpr("FALSE")))).isTrue();
    assertThat(AttributeUtils.isFalse(attr(booleanExpr("TRUE")))).isFalse();
    assertThat(AttributeUtils.isFalse(attr(booleanExpr("false")))).isTrue();
    assertThat(AttributeUtils.isFalse(attr(stringExpr("false")))).isFalse();
    assertThat(AttributeUtils.isFalse(attr(new VariableExprTreeImpl(null)))).isFalse();
  }

  private static AttributeTree attr(ExpressionTree value) {
    return attribute().value(value).build();
  }

}
