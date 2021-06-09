/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.testing.TextRangeAssert.assertTextRange;

class SequenceTreeImplTest extends CloudformationTreeTest {

  @Test
  void simple_sequence() {
    SequenceTree tree = (SequenceTree) parse("[1, \"a\"]").root();
    assertThat(tree).satisfies(s -> {
      assertThat(s.elements()).hasSize(2);
      // TODO: start and end columns are wrong, it is not taking into account the brackets
      assertTextRange(s.textRange()).hasRange(1, 1, 1, 7);
      assertThat(s.elements().get(0)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.PLAIN));
      assertThat(s.elements().get(1)).isInstanceOfSatisfying(ScalarTree.class, e -> assertThat(e.style()).isEqualTo(ScalarTree.Style.DOUBLE_QUOTED));
    });
  }
}
