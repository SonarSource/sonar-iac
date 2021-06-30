/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.TestTreeBuilders.ObjectBuilder;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.tree.impl.ObjectElementTreeImpl;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.SyntaxTokenBuilder.token;

class ObjectUtilsTest {

  private static final ExpressionTree value = booleanExpr("false");
  private static final ObjectTree object = ObjectBuilder.object()
    .element("key1", value)
    .element(new ObjectElementTreeImpl(stringExpr("key2"), token(":"), value))
    .build();

  @Test
  void test_getElement() {
    assertThat(ObjectUtils.getElement(object, "key1")).isPresent();
    assertThat(ObjectUtils.getElement(object, "key3")).isNotPresent();
  }

  @Test
  void test_getElementValue() {
    assertThat(ObjectUtils.getElementValue(object, "key1")).isPresent().get().isEqualTo(value);
    assertThat(ObjectUtils.getElementValue(object, "key3")).isNotPresent();
    assertThat(ObjectUtils.getElementValue((ExpressionTree) object, "key1")).isPresent();
    assertThat(ObjectUtils.getElementValue(new VariableExprTreeImpl(null), "key1")).isNotPresent();
  }
}
