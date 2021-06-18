/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.TextRangeAssert.assertTextRange;

class SequenceTreeImplTest extends CloudformationTreeTest {

  @Test
  void simple_sequence() {
    SequenceTree tree = (SequenceTree) parse("[1, \"a\"]").root();
    assertThat(tree.elements()).hasSize(2);
    assertTextRange(tree.textRange()).hasRange(1, 0, 1, 8);
    assertThat(tree.elements().get(0)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.PLAIN));
    assertThat(tree.elements().get(1)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED));
  }
}
