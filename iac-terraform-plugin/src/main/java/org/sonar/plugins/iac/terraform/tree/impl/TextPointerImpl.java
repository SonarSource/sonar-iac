/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.tree.impl;

import java.util.Objects;
import org.sonar.plugins.iac.terraform.api.tree.TextPointer;

public class TextPointerImpl implements TextPointer {

  private final int line;
  private final int column;

  public TextPointerImpl(int line, int column) {
    this.line = line;
    this.column = column;
  }

  @Override
  public int line() {
    return line;
  }

  @Override
  public int column() {
    return column;
  }

  @Override
  public int compareTo(TextPointer other) {
    int lineCompare = Integer.compare(this.line(), other.line());
    if (lineCompare != 0) {
      return lineCompare;
    }
    return Integer.compare(this.column(), other.column());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextPointerImpl that = (TextPointerImpl) o;
    return line == that.line && column == that.column;
  }

  @Override
  public int hashCode() {
    return Objects.hash(line, column);
  }

}
