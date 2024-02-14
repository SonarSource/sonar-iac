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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.sonar.iac.common.api.tree.impl.TextRanges.toPositionAndLength;

class TextRangesTest {

  private static String TEXT = "line1\n" +
    "line 2 some text\n" +
    "line 3 extra text";

  @Test
  void test_range() {
    TextRange range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(TextRangesTest.range(1, 2, 1, 7));
  }

  @Test
  void test_merge() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    TextRange range2 = TextRangesTest.range(5, 6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(TextRangesTest.range(1, 2, 7, 8));
  }

  @Test
  void test_merge_single() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    assertThat(TextRanges.merge(Collections.singletonList(range1)))
      .isEqualTo(TextRangesTest.range(1, 2, 3, 4));
  }

  @Test
  void test_merge_no_range() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> TextRanges.merge(Collections.emptyList()))
      .withMessage("Can't merge 0 ranges");
  }

  @Test
  void mergeElementsWithTextRange() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    TextRange range2 = TextRangesTest.range(5, 6, 7, 8);

    HasTextRange hasTextRange1 = () -> range1;
    HasTextRange hasTextRange2 = () -> range2;

    TextRange expectedRange = TextRanges.merge(List.of(range1, range2));

    assertThat(TextRanges.mergeElementsWithTextRange(List.of(hasTextRange1, hasTextRange2))).isEqualTo(expectedRange);
  }

  @Test
  void mergeElementsWithTextRangeSingle() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    HasTextRange hasTextRange1 = () -> range1;

    TextRange expectedRange = TextRanges.merge(Collections.singletonList(range1));

    assertThat(TextRanges.mergeElementsWithTextRange(List.of(hasTextRange1))).isEqualTo(expectedRange);
  }

  @Test
  void mergeElementsWithTextRangeNoRange() {
    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> TextRanges.mergeElementsWithTextRange(Collections.emptyList()))
      .withMessage("Can't merge 0 ranges");
  }

  @Test
  void isValidAndNotEmpty() {
    assertThat(TextRanges.isValidAndNotEmpty(range(1, 1, 1, 1))).isFalse();
    assertThat(TextRanges.isValidAndNotEmpty(range(2, 1, 1, 1))).isFalse();
    assertThat(TextRanges.isValidAndNotEmpty(range(1, 2, 3, 4))).isTrue();
  }

  @Test
  void shouldConvertToPositionAndLengthFirstLine() {
    var range = range(1, 0, 1, 5);
    var positionAndLength = toPositionAndLength(range, TEXT);
    assertThat(positionAndLength).isEqualTo(new Tuple<>(0, 5));
  }

  @Test
  void shouldConvertToPositionAndLengthSecondLine() {
    var range = range(2, 0, 2, 11);
    var positionAndLength = toPositionAndLength(range, TEXT);
    assertThat(positionAndLength).isEqualTo(new Tuple<>(6, 11));
  }

  @Test
  void shouldConvertToPositionAndLengthLastLine() {
    var range = range(3, 1, 3, 17);
    var positionAndLength = toPositionAndLength(range, TEXT);
    assertThat(positionAndLength).isEqualTo(new Tuple<>(24, 17));
  }

  @Test
  void shouldConvertToPositionAndLengthFirstAndSecondLine() {
    var range = range(1, 0, 2, 7);
    var positionAndLength = toPositionAndLength(range, TEXT);
    assertThat(positionAndLength).isEqualTo(new Tuple<>(0, 13));
  }

  @Test
  void shouldConvertToPositionAndLengthFirstToThirdLine() {
    var range = range(1, 0, 3, 10);
    var positionAndLength = toPositionAndLength(range, TEXT);
    assertThat(positionAndLength).isEqualTo(new Tuple<>(0, 33));
  }

  public static TextRange range(int startLine, int startLineColumn, int endLine, int endLineColumn) {
    return new TextRange(new TextPointer(startLine, startLineColumn), new TextPointer(endLine, endLineColumn));
  }
}
