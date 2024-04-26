/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.springconfig.tree.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    SyntaxTokenImpl keySyntaxToken = new SyntaxTokenImpl("keySyntaxToken", TextRanges.range(1, 4, 1, 10));
    ScalarImpl keyScalar = new ScalarImpl(keySyntaxToken);
    SyntaxTokenImpl valueSyntaxToken = new SyntaxTokenImpl("valueSyntaxToken", TextRanges.range(1, 11, 1, 20));
    ScalarImpl valueScalar = new ScalarImpl(valueSyntaxToken);

    TupleImpl tupleTree = new TupleImpl(keyScalar, valueScalar);

    SyntaxTokenImpl keySyntaxToken2 = new SyntaxTokenImpl("keySyntaxToken", TextRanges.range(3, 2, 3, 15));
    ScalarImpl keyScalar2 = new ScalarImpl(keySyntaxToken2);
    SyntaxTokenImpl valueSyntaxToken2 = new SyntaxTokenImpl("valueSyntaxToken", TextRanges.range(3, 17, 3, 30));
    ScalarImpl valueScalar2 = new ScalarImpl(valueSyntaxToken2);

    TupleImpl tupleTree2 = new TupleImpl(keyScalar2, valueScalar2);

    ProfileImpl profileTree = new ProfileImpl(List.of(tupleTree, tupleTree2));

    assertThat(profileTree.properties()).containsExactly(tupleTree, tupleTree2);
    assertThat(profileTree.children()).containsExactly(tupleTree, tupleTree2);
    assertThat(profileTree.comments()).isEmpty();
    assertThat(profileTree.name()).isEqualTo("default");
    assertThat(profileTree.isActive()).isTrue();

    TextRangeAssert.assertThat(profileTree.textRange())
      .hasRange(1, 4, 3, 30);
  }

  @Test
  void constructorTestWhenEmpty() {
    ProfileImpl profileTree = new ProfileImpl(List.of());

    assertThat(profileTree.properties()).isEmpty();
    assertThat(profileTree.children()).isEmpty();
    assertThat(profileTree.comments()).isEmpty();
    assertThat(profileTree.name()).isEqualTo("default");
    assertThat(profileTree.isActive()).isTrue();
    assertThatThrownBy(profileTree::textRange)
      .withFailMessage("Can't merge 0 ranges")
      .isInstanceOf(IllegalArgumentException.class);
  }
}
