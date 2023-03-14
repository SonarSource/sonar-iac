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

import java.util.Objects;

public class TextRange {

  private final TextPointer start;
  private final TextPointer end;

  public TextRange(TextPointer start, TextPointer end) {
    this.start = start;
    this.end = end;
  }

  public TextPointer start() {
    return start;
  }

  public TextPointer end() {
    return end;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    TextRange otherRange = (TextRange) other;
    return Objects.equals(start, otherRange.start) && Objects.equals(end, otherRange.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(start, end);
  }
}
