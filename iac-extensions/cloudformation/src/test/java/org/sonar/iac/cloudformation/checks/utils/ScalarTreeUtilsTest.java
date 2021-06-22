/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks.utils;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.tree.impl.MappingTreeImpl;
import org.sonar.iac.cloudformation.tree.impl.ScalarTreeImpl;

import static org.assertj.core.api.Assertions.assertThat;

class ScalarTreeUtilsTest {

  @Test
  void test_simple_scalar() {
    ScalarTree scalarTree = new ScalarTreeImpl("foo", null, null, null, Collections.emptyList());
    assertThat(ScalarTreeUtils.getValue(scalarTree)).isPresent().get().isEqualTo("foo");
  }

  @Test
  void test_without_scalar() {
    MappingTree mappingTree = new MappingTreeImpl(Collections.emptyList(), null, null, Collections.emptyList());
    assertThat(ScalarTreeUtils.getValue(mappingTree)).isNotPresent();
  }
}
