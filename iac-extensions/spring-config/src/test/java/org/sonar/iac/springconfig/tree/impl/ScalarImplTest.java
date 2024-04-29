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

class ScalarImplTest {

  @Test
  void constructorShouldFunctionCorrectly() {
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl("testValue", TextRanges.range(1, 2, 3, 4));
    ScalarImpl scalarTree = new ScalarImpl(syntaxToken);

    assertThat(scalarTree.value()).isEqualTo(syntaxToken);
    assertThat(scalarTree.children()).hasSize(1);
    assertThat(scalarTree.children()).containsOnly(syntaxToken);

    TextRangeAssert.assertThat(scalarTree.textRange())
      .hasRange(1, 2, 3, 4);
  }

  @Test
  void constructorTestWhenNull() {
    ScalarImpl scalarTree = new ScalarImpl(null);

    assertThat(scalarTree.value()).isNull();
    assertThatThrownBy(scalarTree::children).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(scalarTree::textRange).isInstanceOf(NullPointerException.class);
  }
}
