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

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

import static org.assertj.core.api.Assertions.assertThat;

class CompoundTextRangeTest {

  @Test
  void equals() {
    TextRange range = TextRanges.range(1, 2, 3, 4);
    TextRange sameRange = TextRanges.range(1, 2, 3, 4);
    TextRange differentRange = TextRanges.range(5, 6, 7, 8);
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
}
