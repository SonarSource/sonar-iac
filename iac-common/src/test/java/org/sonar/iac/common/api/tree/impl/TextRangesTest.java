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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.common.api.tree.HasTextRange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TextRangesTest {

  @Test
  void shouldCreateRangeUsingString() {
    var range = TextRanges.range(1, 2, "value");
    assertThat(range).isEqualTo(TextRangesTest.range(1, 2, 1, 7));
  }

  @Test
  void shouldMerge2Ranges() {
    var range1 = TextRangesTest.range(1, 2, 3, 4);
    var range2 = TextRangesTest.range(5, 6, 7, 8);
    assertThat(TextRanges.merge(Arrays.asList(range1, range2)))
      .isEqualTo(TextRangesTest.range(1, 2, 7, 8));
  }

  @Test
  void shouldMerge2RangesVarArg() {
    var range1 = TextRangesTest.range(1, 2, 3, 4);
    var range2 = TextRangesTest.range(5, 6, 7, 8);
    assertThat(TextRanges.merge(range1, range2))
      .isEqualTo(TextRangesTest.range(1, 2, 7, 8));
  }

  @Test
  void shouldMergeSingleRange() {
    var range1 = TextRangesTest.range(1, 2, 3, 4);
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
    var range1 = TextRangesTest.range(1, 2, 3, 4);
    var range2 = TextRangesTest.range(5, 6, 7, 8);

    HasTextRange hasTextRange1 = () -> range1;
    HasTextRange hasTextRange2 = () -> range2;

    var expectedRange = TextRanges.merge(List.of(range1, range2));

    assertThat(TextRanges.mergeElementsWithTextRange(List.of(hasTextRange1, hasTextRange2))).isEqualTo(expectedRange);
  }

  @Test
  void mergeElementsWithTextRangeSingle() {
    var range1 = TextRangesTest.range(1, 2, 3, 4);
    HasTextRange hasTextRange1 = () -> range1;

    var expectedRange = TextRanges.merge(Collections.singletonList(range1));

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

  @ParameterizedTest
  @MethodSource("endsBeforeAnotherEndsTestCases")
  void shouldCheckIfTargetEndsBeforeBase(int targetEndLine, int targetEndOffset, int baseEndLine, int baseEndOffset, boolean expected) {
    var target = createHasTextRange(1, 0, targetEndLine, targetEndOffset);
    var base = createHasTextRange(1, 0, baseEndLine, baseEndOffset);

    assertThat(TextRanges.endsBeforeAnotherEnds(target, base)).isEqualTo(expected);
  }

  private static Stream<Arguments> endsBeforeAnotherEndsTestCases() {
    return Stream.of(
      arguments(2, 0, 3, 0, true), // target ends before base
      arguments(2, 5, 2, 5, true), // target ends at same position as base
      arguments(4, 0, 3, 0, false), // target ends after base
      arguments(2, 3, 2, 10, true) // target ends before base on same line
    );
  }

  @ParameterizedTest
  @MethodSource("comparingTextRangeStartTestCases")
  void shouldCompareElementsByTextRangeStart(List<HasTextRange> input, List<HasTextRange> expected) {
    var comparator = TextRanges.<HasTextRange>comparingTextRangeStart(e -> e);
    var sorted = input.stream()
      .sorted(comparator)
      .toList();

    assertThat(sorted).containsExactly(expected.toArray(new HasTextRange[0]));
  }

  private static Stream<Arguments> comparingTextRangeStartTestCases() {
    var line3 = createHasTextRange(3, 0, 3, 10);
    var line1 = createHasTextRange(1, 0, 1, 10);
    var line2 = createHasTextRange(2, 0, 2, 10);

    var offset10 = createHasTextRange(1, 10, 1, 20);
    var offset5 = createHasTextRange(1, 5, 1, 15);
    var offset15 = createHasTextRange(1, 15, 1, 25);

    var line2offset5 = createHasTextRange(2, 5, 2, 10);
    var line1offset10 = createHasTextRange(1, 10, 1, 20);
    var line2offset3 = createHasTextRange(2, 3, 2, 8);
    var line1offset5 = createHasTextRange(1, 5, 1, 15);

    return Stream.of(
      arguments(List.of(line3, line1, line2), List.of(line1, line2, line3)),
      arguments(List.of(offset10, offset5, offset15), List.of(offset5, offset10, offset15)),
      arguments(List.of(line2offset5, line1offset10, line2offset3, line1offset5), List.of(line1offset5, line1offset10, line2offset3, line2offset5)));
  }

  @Test
  void shouldCompareElementsWithMapper() {
    record Wrapper(HasTextRange range) {
    }

    var wrapper1 = new Wrapper(createHasTextRange(3, 0, 3, 10));
    var wrapper2 = new Wrapper(createHasTextRange(1, 0, 1, 10));
    var wrapper3 = new Wrapper(createHasTextRange(2, 0, 2, 10));

    var comparator = TextRanges.comparingTextRangeStart(Wrapper::range);
    var sorted = List.of(wrapper1, wrapper2, wrapper3).stream()
      .sorted(comparator)
      .toList();

    assertThat(sorted).containsExactly(wrapper2, wrapper3, wrapper1);
  }

  private static HasTextRange createHasTextRange(int startLine, int startColumn, int endLine, int endColumn) {
    return () -> range(startLine, startColumn, endLine, endColumn);
  }

  public static TextRange range(int startLine, int startLineColumn, int endLine, int endLineColumn) {
    return new TextRange(new TextPointer(startLine, startLineColumn), new TextPointer(endLine, endLineColumn));
  }
}
