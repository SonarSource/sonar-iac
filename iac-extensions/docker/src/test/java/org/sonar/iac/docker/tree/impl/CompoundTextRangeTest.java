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
package org.sonar.iac.docker.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class CompoundTextRangeTest {

  @Test
  void shouldReturnCorrectIndexForSingleLine() {
    var ranges = ranges(code("this test is good"));
    CompoundTextRange compoundTextRange = new CompoundTextRange(ranges);

    TextRange range = compoundTextRange.computeTextRangeAtIndex(5, "test");
    assertRange(range, 1, 5, 1, 9);
  }

  @Test
  void shouldReturnCorrectIndexForEmptyValue() {
    var ranges = ranges(code("this test is good"));
    CompoundTextRange compoundTextRange = new CompoundTextRange(ranges);

    TextRange range = compoundTextRange.computeTextRangeAtIndex(5, "");
    assertRange(range, 1, 5, 1, 5);
  }

  @Test
  void shouldReturnCorrectIndexForTwoLine() {
    var ranges = ranges(code(
      "this test_",
      "value is good"));
    CompoundTextRange compoundTextRange = new CompoundTextRange(ranges);

    TextRange range1 = compoundTextRange.computeTextRangeAtIndex(5, "test_");
    assertRange(range1, 1, 5, 1, 10);
    TextRange range2 = compoundTextRange.computeTextRangeAtIndex(11, "value");
    assertRange(range2, 2, 0, 2, 5);
  }

  @Test
  void shouldReturnCorrectIndexStartingNotOnFirstLine() {
    var ranges = ranges(code(
      "multi line code",
      "with test value"));
    CompoundTextRange compoundTextRange = new CompoundTextRange(ranges);

    TextRange range = compoundTextRange.computeTextRangeAtIndex(21, "test");
    assertRange(range, 2, 5, 2, 9);
  }

  @Test
  void shouldReturnCorrectIndexForMultiLineWithEmptyLines() {
    var ranges = ranges(code(
      "this test_",
      "",
      "",
      "value in my code"));
    CompoundTextRange compoundTextRange = new CompoundTextRange(ranges);

    TextRange range1 = compoundTextRange.computeTextRangeAtIndex(5, "test_");
    assertRange(range1, 1, 5, 1, 10);
    TextRange range2 = compoundTextRange.computeTextRangeAtIndex(13, "value");
    assertRange(range2, 4, 0, 4, 5);
  }

  @Test
  void shouldEqualsToItselfOrEquivalent() {
    TextRange range = range(1, 2, 3, 4);
    TextRange sameRange = range(1, 2, 3, 4);
    TextRange differentRange = range(5, 6, 7, 8);
    CompoundTextRange compoundTextRange = new CompoundTextRange(List.of(range));
    CompoundTextRange compoundTextRangeSame = new CompoundTextRange(List.of(sameRange));
    CompoundTextRange compoundTextRangeDifferent = new CompoundTextRange(List.of(differentRange));
    CompoundTextRange compoundTextRangeDifferent2 = new CompoundTextRange(List.of(sameRange, differentRange));
    CompoundTextRange compoundTextRangeEmpty = new CompoundTextRange(List.of(differentRange));

    assertThat(compoundTextRange)
      .isEqualTo(compoundTextRange)
      .isEqualTo(compoundTextRangeSame)
      .isNotEqualTo(compoundTextRangeDifferent)
      .isNotEqualTo(compoundTextRangeDifferent2)
      .isNotEqualTo(compoundTextRangeEmpty)
      .isNotEqualTo(null)
      .isNotEqualTo(new Object());
  }

  List<TextRange> ranges(String code) {
    List<TextRange> ranges = new ArrayList<>();
    int line = 1;
    int column = 0;
    for (char c : code.toCharArray()) {
      if (c == '\n') {
        ranges.add(TextRanges.range(line, 0, line, column));
        line++;
        column = 0;
      } else {
        column++;
      }
    }
    ranges.add(TextRanges.range(line, 0, line, column));
    return ranges;
  }

  void assertRange(TextRange range, int startLine, int startOffset, int endLine, int endOffset) {
    assertThat(range.start().line()).withFailMessage("Invalid start line: expected %s, got %s", startLine, range.start().line()).isEqualTo(startLine);
    assertThat(range.start().lineOffset()).withFailMessage("Invalid start line offset: expected %s, got %s", startLine, range.start().lineOffset()).isEqualTo(startOffset);
    assertThat(range.end().line()).withFailMessage("Invalid end line: expected %s, got %s", startLine, range.end().line()).isEqualTo(endLine);
    assertThat(range.end().lineOffset()).withFailMessage("Invalid end line offset: expected %s, got %s", startLine, range.end().lineOffset()).isEqualTo(endOffset);
  }
}
