/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks.utils;

import org.junit.jupiter.api.Test;
import org.sonar.iac.terraform.TestTreeBuilders.ObjectBuilder;
import org.sonar.iac.terraform.api.tree.ObjectTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.terraform.TestTreeBuilders.LiteralExprBuilder.booleanExpr;

class ObjectUtilsTest {

  @Test
  void test_getElement() {
    ObjectTree object = ObjectBuilder.object().element("key1", booleanExpr("true")).build();

    assertThat(ObjectUtils.getElement(object, "key1")).isPresent();
    assertThat(ObjectUtils.getElement(object, "key3")).isNotPresent();
  }
}
