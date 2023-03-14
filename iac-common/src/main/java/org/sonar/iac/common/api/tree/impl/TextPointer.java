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

public class TextPointer implements Comparable<TextPointer> {

  private final int line;
  private final int lineOffset;

  public TextPointer(int line, int lineOffset) {
    this.line = line;
    this.lineOffset = lineOffset;
  }

  public int line() {
    return line;
  }

  public int lineOffset() {
    return lineOffset;
  }

  @Override
  public int compareTo(TextPointer other) {
    if (this.line == other.line()) {
      return Integer.compare(this.lineOffset, other.lineOffset());
    }
    return Integer.compare(this.line, other.line());
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    TextPointer otherPointer = (TextPointer) other;
    return line == otherPointer.line && lineOffset == otherPointer.lineOffset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(line, lineOffset);
  }
}
