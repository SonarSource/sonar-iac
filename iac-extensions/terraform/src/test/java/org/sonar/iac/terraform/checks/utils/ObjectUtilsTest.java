/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.TestTreeBuilders.ObjectBuilder;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.tree.impl.ObjectElementTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.stringExpr;
import static org.sonar.iac.terraform.TestTreeBuilders.SyntaxTokenBuilder.token;

class ObjectUtilsTest {

  @Test
  void test_getElement() {
    ObjectTree object = ObjectBuilder.object()
      .element("key1", booleanExpr("true"))
      .element(new ObjectElementTreeImpl(stringExpr("key2"), token(":"), booleanExpr("false")))
      .build();

    assertThat(ObjectUtils.getElement(object, "key1")).isPresent();
    assertThat(ObjectUtils.getElement(object, "key3")).isNotPresent();
  }
}
