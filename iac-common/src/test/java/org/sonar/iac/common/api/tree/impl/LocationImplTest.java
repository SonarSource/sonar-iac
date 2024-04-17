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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.common.api.tree.impl.TextRanges.range;
import static org.sonar.iac.common.api.tree.impl.LocationImpl.fromTextRange;
import static org.sonar.iac.common.testing.IacCommonAssertions.assertThat;

class LocationImplTest {

  private final static String TEXT = "line1\n" +
    "line 2 some text\n" +
    "line 3 extra text";

  @Test
  void shouldReturnPositionAndLength() {
    var position = new LocationImpl(1, 2);
    assertThat(position).hasLocation(1, 2);
  }

  @Test
  void shouldVerifyEqualsAndHashCode() {
    var location1 = new LocationImpl(1, 2);
    var location2 = new LocationImpl(1, 2);
    var location3 = new LocationImpl(3, 4);
    var location4 = new LocationImpl(1, 4);

    assertThat(location1).isEqualTo(location1)
      .hasSameHashCodeAs(location1)
      .isEqualTo(location2)
      .hasSameHashCodeAs(location2)
      .isNotEqualTo(location3)
      .isNotEqualTo(location4)
      .doesNotHaveSameHashCodeAs(location3)
      .isNotEqualTo("dummy")
      .isNotEqualTo(null);
  }

  @Test
  void shouldVerifyToString() {
    var location = new LocationImpl(10, 20);
    assertThat(location).hasToString("LocationImpl{position=10, length=20}");
  }

  @Test
  void shouldConvertToLocationFirstLine() {
    var range = range(1, 0, 1, 5);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("line1")
      .hasLocation(0, 5);
  }

  @Test
  void shouldConvertToTextRangeFirstLine() {
    var location = new LocationImpl(0, 5);
    assertThat(location.toTextRange(TEXT))
      .hasRange(1, 0, 1, 5);
  }

  @Test
  void shouldConvertToLocationSecondLine() {
    var range = range(2, 0, 2, 11);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("line 2 some")
      .hasLocation(6, 11);
  }

  @Test
  void shouldConvertToTextRangeSecondLine() {
    var location = new LocationImpl(6, 11);
    assertThat(location.toTextRange(TEXT))
      .hasRange(2, 0, 2, 11);
  }

  @Test
  void shouldConvertToLocationSecondLineStartColumnIsThree() {
    var range = range(2, 3, 2, 11);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("e 2 some")
      .hasLocation(9, 8);
  }

  @Test
  void shouldConvertToTextRangeSecondLineStartLineOffsetIsThree() {
    var location = new LocationImpl(9, 8);
    assertThat(location.toTextRange(TEXT))
      .hasRange(2, 3, 2, 11);
  }

  @Test
  void shouldConvertToLocationLastLine() {
    var range = range(3, 1, 3, 17);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("ine 3 extra text")
      .hasLocation(24, 16);
  }

  @Test
  void shouldConvertToTextRangeLastLine() {
    var location = new LocationImpl(24, 16);
    assertThat(location.toTextRange(TEXT))
      .hasRange(3, 1, 3, 17);
  }

  @Test
  void shouldConvertToLocationFirstAndSecondLine() {
    var range = range(1, 0, 2, 7);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("line1\nline 2 ")
      .hasLocation(0, 13);
  }

  @Test
  void shouldConvertToTextRangeAndSecondLine() {
    var location = new LocationImpl(0, 13);
    assertThat(location.toTextRange(TEXT))
      .hasRange(1, 0, 2, 7);
  }

  @Test
  void shouldConvertToLocationFirstToThirdLine() {
    var range = range(1, 0, 3, 10);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("line1\n" +
        "line 2 some text\n" +
        "line 3 ext")
      .hasLocation(0, 33);
  }

  @Test
  void shouldConvertToTextRangeFirstToThirdLine() {
    var location = new LocationImpl(0, 33);
    assertThat(location.toTextRange(TEXT))
      .hasRange(1, 0, 3, 10);
  }

  @Test
  void shouldConvertToLocationWholeText() {
    var range = range(1, 0, 3, 17);
    var location = fromTextRange(range, TEXT);
    assertThat(location).on(TEXT)
      .isEqualTo("line1\n" +
        "line 2 some text\n" +
        "line 3 extra text")
      .hasLocation(0, 40);
  }

  @Test
  void shouldConvertToTextRangeWholeText() {
    var location = new LocationImpl(0, 40);
    assertThat(location.toTextRange(TEXT))
      .hasRange(1, 0, 3, 17);
  }

  @Test
  void shouldThrowExceptionWhenStartLineNumberDoesntExist() {
    var range = range(4, 0, 4, 1);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenStartPositionDoesntExist() {
    var location = new LocationImpl(TEXT.length() + 1, 0);
    assertThatThrownBy(() -> location.toTextRange(TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenEndPositionDoesntExist() {
    var location = new LocationImpl(0, TEXT.length() + 1);
    assertThatThrownBy(() -> location.toTextRange(TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenStartLineNumberIsSix() {
    var range = range(6, 0, 6, 1);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenStartLineColumnIsTooBig() {
    var range = range(3, 18, 3, 20);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenEndLineNumberDoesntExist() {
    var range = range(2, 0, 4, 0);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenEndLineOffsetIsTooBig() {
    var range = range(2, 0, 2, 100);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionWhenEndLineColumnIsTooBig() {
    var range = range(3, 0, 3, 18);
    assertThatThrownBy(() -> fromTextRange(range, TEXT))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionTextRangeWhenEmptyText() {
    var range = range(2, 0, 2, 10);
    assertThatThrownBy(() -> fromTextRange(range, ""))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldThrowExceptionLocationWhenEmptyText() {
    var location = new LocationImpl(0, 1);
    assertThatThrownBy(() -> location.toTextRange(""))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
