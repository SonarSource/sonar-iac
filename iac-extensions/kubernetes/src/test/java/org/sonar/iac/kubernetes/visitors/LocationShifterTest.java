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
package org.sonar.iac.kubernetes.visitors;

import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.common.extension.visitors.InputFileContext;

import static org.mockito.Mockito.mock;
import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class LocationShifterTest {

  private final LocationShifter shifter = new LocationShifter();
  private final InputFileContext ctx = mock(InputFileContext.class);

  @Test
  void shouldReturnShiftedRange() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldReturnOriginalRangeIfNoShiftStored() {
    setLinesSizes(ctx, 5);
    TextRange originalRange = TextRanges.range(1, 1, 1, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, originalRange);
    assertThat(shiftedRange).isSameAs(originalRange);
  }

  @Test
  void shouldOverridePreviousShift() {
    setLinesSizes(ctx, 5, 10);
    shifter.addShiftedLine(ctx, 1, 5);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldKeepAllShifts() {
    setLinesSizes(ctx, 5, 10, 15, 20);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 4);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(4, 0, 4, 20);
  }

  @Test
  void shouldAllowSameTargetLine() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 3);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(ctx, TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(3, 0, 3, 15);
  }

  @Test
  void shouldShiftStartRangeOnlyForRegisteredLineShift() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 3, 3));
    assertThat(shiftedRange).hasRange(2, 0, 3, 3);
  }

  @Test
  void shouldShiftEndRangeOnlyForRegisteredLineShift() {
    setLinesSizes(ctx, 5, 10, 15);
    shifter.addShiftedLine(ctx, 2, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(1, 1, 3, 15);
  }

  @Test
  void shouldProvideShiftedRangeOnMultipleLines() {
    setLinesSizes(ctx, 5, 10, 15, 20);
    shifter.addShiftedLine(ctx, 1, 3);
    shifter.addShiftedLine(ctx, 2, 4);
    TextRange shiftedRange = shifter.computeShiftedLocation(ctx, TextRanges.range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(3, 0, 4, 20);
  }

  @Test
  void shouldNotAccessShiftedRangeOfDifferentContext() {
    InputFileContext ctx1 = mock(InputFileContext.class);
    InputFileContext ctx2 = mock(InputFileContext.class);

    setLinesSizes(ctx1, 5, 10, 15);
    setLinesSizes(ctx2, 6, 11, 16, 21);
    shifter.addShiftedLine(ctx1, 1, 3);
    shifter.addShiftedLine(ctx2, 2, 4);

    TextRange shiftedRangeCtx1_1 = shifter.computeShiftedLocation(ctx1, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRangeCtx1_1).hasRange(3, 0, 3, 15);
    TextRange shiftedRangeCtx1_2 = shifter.computeShiftedLocation(ctx1, TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRangeCtx1_2).hasRange(2, 1, 2, 3);

    TextRange shiftedRangeCtx2_1 = shifter.computeShiftedLocation(ctx2, TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRangeCtx2_1).hasRange(1, 1, 1, 3);
    TextRange shiftedRangeCtx2_2 = shifter.computeShiftedLocation(ctx2, TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRangeCtx2_2).hasRange(4, 0, 4, 21);
  }

  void setLinesSizes(InputFileContext ctx, int... linesSizes) {
    for (int lineNumber = 1; lineNumber <= linesSizes.length; lineNumber++) {
      shifter.addLineSize(ctx, lineNumber, linesSizes[lineNumber - 1]);
    }
  }
}
