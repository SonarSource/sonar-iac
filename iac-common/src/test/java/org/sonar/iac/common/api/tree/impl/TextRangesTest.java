/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class TextRangesTest {

  @Test
  void test_range() {
    TextRange range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(TextRangesTest.range(1,2, 1, 7));
  }

  @Test
  void test_merge() {
    TextRange range1 = TextRangesTest.range(1,2, 3, 4);
    TextRange range2 = TextRangesTest.range(5,6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(TextRangesTest.range(1,2, 7, 8));
  }

  @Test
  void test_merge_single() {
    TextRange range1 = TextRangesTest.range(1,2, 3, 4);
    assertThat(TextRanges.merge(Collections.singletonList(range1)))
      .isEqualTo(TextRangesTest.range(1,2, 3, 4));
  }

  @Test
  void test_merge_no_range() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> TextRanges.merge(Collections.emptyList()))
      .withMessage("Can't merge 0 ranges");
  }

  @Test
  void isValidAndNotEmpty() {
    assertThat(TextRanges.isValidAndNotEmpty(range(1, 1, 1, 1))).isFalse();
    assertThat(TextRanges.isValidAndNotEmpty(range(2, 1, 1, 1))).isFalse();
    assertThat(TextRanges.isValidAndNotEmpty(range(1, 2, 3, 4))).isTrue();
  }

  public static TextRange range(int startLine, int startLineColumn, int endLine, int endLineColumn) {
    return new TextRange(new TextPointer(startLine, startLineColumn), new TextPointer(endLine, endLineColumn));
  }
}
