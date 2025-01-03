/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.api.tree.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.HasTextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class TextRangesTest {

  @Test
  void shouldCreateRangeUsingString() {
    TextRange range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(TextRangesTest.range(1, 2, 1, 7));
  }

  @Test
  void shouldMerge2Ranges() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    TextRange range2 = TextRangesTest.range(5, 6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(TextRangesTest.range(1, 2, 7, 8));
  }

  @Test
  void shouldMerge2RangesVarArg() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    TextRange range2 = TextRangesTest.range(5, 6, 7, 8);
    assertThat(TextRanges.merge(range1, range2))
      .isEqualTo(TextRangesTest.range(1, 2, 7, 8));
  }

  @Test
  void shouldMergeSingleRange() {
    TextRange range1 = TextRangesTest.range(1, 2, 3, 4);
    assertThat(TextRanges.merge(Collections.singletonList(range1)))
      .isEqualTo(TextRangesTest.range(1, 2, 3, 4));
  }

  @Test
  void shouldThrownExceptionWhenMergeNoRange() {
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

  public static TextRange range(int startLine, int startLineColumn, int endLine, int endLineColumn) {
    return new TextRange(new TextPointer(startLine, startLineColumn), new TextPointer(endLine, endLineColumn));
  }
}
