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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TextPointerTest {

  @Test
  void equals() {
    TextPointer pointer = new TextPointer(1, 2);
    TextPointer samePointer = new TextPointer(1, 2);
    TextPointer sameLineOtherOffset = new TextPointer(1, 3);
    TextPointer otherLineSameOffset = new TextPointer(4, 2);
    Object notATextPointer = new Object();

    assertThat(pointer)
      .isEqualTo(pointer)
      .isNotEqualTo(null)
      .isNotEqualTo(notATextPointer)
      .isEqualTo(samePointer)
      .isNotEqualTo(sameLineOtherOffset)
      .isNotEqualTo(otherLineSameOffset);
  }

  @Test
  void compareTo() {
    TextPointer pointer = new TextPointer(1, 2);
    TextPointer samePointer = new TextPointer(1, 2);
    TextPointer smallerLine = new TextPointer(0, 2);
    TextPointer smallerColumn = new TextPointer(1, 1);

    assertThat(pointer)
      .isEqualByComparingTo(samePointer)
      .isGreaterThan(smallerLine)
      .isGreaterThan(smallerColumn);
  }
}
