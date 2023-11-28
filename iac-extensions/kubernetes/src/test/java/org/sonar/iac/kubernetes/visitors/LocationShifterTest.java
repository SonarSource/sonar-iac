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

import static org.sonar.iac.common.testing.TextRangeAssert.assertThat;

class LocationShifterTest {

  private final LocationShifter shifter = new LocationShifter();

  @Test
  void shouldReturnShiftedRange() {
    setLinesSizes(5, 10);
    shifter.addShiftedLine(1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldReturnOriginalRangeIfNoShiftStored() {
    TextRange originalRange = TextRanges.range(1, 1, 1, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(originalRange);
    assertThat(shiftedRange).isSameAs(originalRange);
  }

  @Test
  void shouldReturnOriginalRangeIfShiftCleared() {
    setLinesSizes(5, 10);
    shifter.addShiftedLine(1, 5);
    shifter.clear();
    TextRange originalRange = TextRanges.range(1, 1, 1, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(originalRange);
    assertThat(shiftedRange).isSameAs(originalRange);
  }

  @Test
  void shouldOverridePreviousShift() {
    setLinesSizes(5, 10);
    shifter.addShiftedLine(1, 5);
    shifter.addShiftedLine(1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange).hasRange(2, 0, 2, 10);
  }

  @Test
  void shouldKeepAllShifts() {
    setLinesSizes(5, 10, 15, 20);
    shifter.addShiftedLine(1, 3);
    shifter.addShiftedLine(2, 4);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(4, 0, 4, 20);
  }

  @Test
  void shouldAllowSameTargetLine() {
    setLinesSizes(5, 10, 15);
    shifter.addShiftedLine(1, 3);
    shifter.addShiftedLine(2, 3);

    TextRange shiftedRange1 = shifter.computeShiftedLocation(TextRanges.range(1, 1, 1, 3));
    assertThat(shiftedRange1).hasRange(3, 0, 3, 15);

    TextRange shiftedRange2 = shifter.computeShiftedLocation(TextRanges.range(2, 1, 2, 3));
    assertThat(shiftedRange2).hasRange(3, 0, 3, 15);
  }

  @Test
  void shouldShiftStartRangeOnlyForRegisteredLineShift() {
    setLinesSizes(5, 10, 15);
    shifter.addShiftedLine(1, 2);
    TextRange shiftedRange = shifter.computeShiftedLocation(TextRanges.range(1, 1, 3, 3));
    assertThat(shiftedRange).hasRange(2, 0, 3, 3);
  }

  @Test
  void shouldShiftEndRangeOnlyForRegisteredLineShift() {
    setLinesSizes(5, 10, 15);
    shifter.addShiftedLine(2, 3);
    TextRange shiftedRange = shifter.computeShiftedLocation(TextRanges.range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(1, 1, 3, 15);
  }

  @Test
  void shouldProvideShiftedRangeOnMultipleLines() {
    setLinesSizes(5, 10, 15, 20);
    shifter.addShiftedLine(1, 3);
    shifter.addShiftedLine(2, 4);
    TextRange shiftedRange = shifter.computeShiftedLocation(TextRanges.range(1, 1, 2, 3));
    assertThat(shiftedRange).hasRange(3, 0, 4, 20);
  }

  void setLinesSizes(int... linesSizes) {
    for (int lineSize : linesSizes) {
      shifter.addLineSize(lineSize);
    }
  }
}
