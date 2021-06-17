/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;

import static org.assertj.core.api.Assertions.assertThat;

class MappingTreeImplTest extends CloudformationTreeTest {

  @Test
  void simple_mapping() {
    MappingTree tree = (MappingTree) parse("a: b").root();
    assertThat(tree.elements()).hasSize(1);
    assertThat(tree.tag()).isEqualTo("tag:yaml.org,2002:map");
    assertThat(tree.elements().get(0)).isInstanceOf(TupleTree.class);
  }
}
