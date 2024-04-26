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
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class SyntaxTokenImplTest {

  @Test
  void constructorTest() {
    TextRange range = TextRanges.range(1, 2, 3, 4);
    String value = "testValue";
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl(value, range);

    assertThat(syntaxToken.value()).isEqualTo(value);
    assertThat(syntaxToken.textRange()).isEqualTo(range);
    assertThat(syntaxToken.children()).isEmpty();
  }

  @Test
  void constructorTestWhenNull() {
    SyntaxTokenImpl syntaxToken = new SyntaxTokenImpl(null, null);

    assertThat(syntaxToken.value()).isNull();
    assertThat(syntaxToken.children()).isEmpty();
    assertThatThrownBy(syntaxToken::textRange)
      .withFailMessage("Can't merge 0 ranges")
      .isInstanceOf(IllegalArgumentException.class);
  }
}
