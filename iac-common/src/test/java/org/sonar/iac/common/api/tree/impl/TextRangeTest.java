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
package org.sonar.iac.common.api.tree.impl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.testing.IacCommonAssertions;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class TextRangeTest {

  @Test
  void equals() {
    TextRange range = new TextRange(new TextPointer(1, 2), new TextPointer(3, 4));
    TextRange sameRange = new TextRange(new TextPointer(1, 2), new TextPointer(3, 4));
    TextRange sameStartOtherEnd = new TextRange(new TextPointer(1, 2), new TextPointer(7, 8));
    TextRange otherStartSameEnd = new TextRange(new TextPointer(5, 6), new TextPointer(3, 4));
    Object notATextRange = new Object();

    assertThat(range)
      .isEqualTo(range)
      .isNotEqualTo(null)
      .isNotEqualTo(notATextRange)
      .isEqualTo(sameRange)
      .isNotEqualTo(sameStartOtherEnd)
      .isNotEqualTo(otherStartSameEnd);
  }

  @Test
  void shouldTrimToTextWhenRangeIsBigger() {
    TextRange range = new TextRange(new TextPointer(1, 0), new TextPointer(1, 10));
    IacCommonAssertions.assertThat(range.trimToText("12345"))
      .hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldTrimToTextWhenRangeIsSmaller() {
    TextRange range = new TextRange(new TextPointer(1, 0), new TextPointer(1, 3));
    IacCommonAssertions.assertThat(range.trimToText("12345"))
      .hasRange(1, 0, 1, 3);
  }

  @Test
  void shouldTrimToTextWhenRangeIsBiggerInLine2() {
    TextRange range = new TextRange(new TextPointer(2, 0), new TextPointer(2, 10));
    IacCommonAssertions.assertThat(range.trimToText("a\n12345"))
      .hasRange(2, 0, 2, 5);
  }

  @Test
  void shouldThrowExceptionWhenLineNumberIsTooBig() {
    TextRange range = new TextRange(new TextPointer(2, 0), new TextPointer(2, 10));
    assertThatThrownBy(() -> range.trimToText("123"))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldTrimToTextWhenRangeIsBiggerAndTextContainsMoreLines() {
    TextRange range = new TextRange(new TextPointer(1, 0), new TextPointer(1, 10));
    IacCommonAssertions.assertThat(range.trimToText("12345\n123"))
      .hasRange(1, 0, 1, 5);
  }
}
