/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;

import static org.assertj.core.api.Assertions.assertThat;

class TupleTreeImplTest extends CloudformationTreeTest {

  @Test
  void simple_tuple() {
    TupleTree tree = ((MappingTree) parse("a: b").root()).elements().get(0);
    assertThat(tree.tag()).isEqualTo("TUPLE");
    assertThat(tree.key()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("a"));
    assertThat(tree.value()).isInstanceOfSatisfying(ScalarTree.class, k -> assertThat(k.value()).isEqualTo("b"));
  }
}
