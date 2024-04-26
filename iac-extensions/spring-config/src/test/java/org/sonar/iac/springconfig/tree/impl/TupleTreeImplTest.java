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

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.testing.TextRangeAssert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TupleTreeImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    SyntaxTokenImpl keySyntaxToken = new SyntaxTokenImpl("keySyntaxToken", TextRanges.range(1, 3, 1, 7));
    ScalarImpl keyScalar = new ScalarImpl(keySyntaxToken);

    SyntaxTokenImpl valueSyntaxToken = new SyntaxTokenImpl("valueSyntaxToken", TextRanges.range(1, 10, 1, 20));
    ScalarImpl valueScalar = new ScalarImpl(valueSyntaxToken);

    TupleImpl tupleTree = new TupleImpl(keyScalar, valueScalar);

    assertThat(tupleTree.key()).isEqualTo(keyScalar);
    assertThat(tupleTree.value()).isEqualTo(valueScalar);

    assertThat(tupleTree.children()).hasSize(2);
    assertThat(tupleTree.children()).containsExactly(keyScalar, valueScalar);

    TextRangeAssert.assertThat(tupleTree.textRange())
      .hasRange(1, 3, 1, 20);
  }

  @Test
  void constructorTestWhenNull() {
    TupleImpl tupleTree = new TupleImpl(null, null);

    assertThat(tupleTree.key()).isNull();
    assertThat(tupleTree.value()).isNull();

    assertThat(tupleTree.children()).hasSize(2);
    assertThat(tupleTree.children()).containsOnlyNulls();

    assertThatThrownBy(tupleTree::textRange).isInstanceOf(NullPointerException.class);
  }
}
