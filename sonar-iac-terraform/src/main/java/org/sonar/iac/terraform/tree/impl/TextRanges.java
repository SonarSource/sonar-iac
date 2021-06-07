/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.terraform.tree.impl;

import java.util.List;
import java.util.function.Supplier;
import org.sonar.iac.terraform.api.tree.TextPointer;
import org.sonar.iac.terraform.api.tree.TextRange;
import org.sonarsource.analyzer.commons.TokenLocation;

import static java.util.Comparator.naturalOrder;

public class TextRanges {

  private static final Supplier<IllegalArgumentException> MERGE_EXCEPTION_SUPPLIER =
    () -> new IllegalArgumentException("Can't merge 0 ranges");

  private TextRanges() {
  }

  public static TextRange range(int line, int column, String value) {
    TokenLocation location = new TokenLocation(line, column, value);
    TextPointer startPointer = new TextPointerImpl(location.startLine(), location.startLineOffset());
    TextPointer endPointer =  new TextPointerImpl(location.endLine(), location.endLineOffset());
    return new TextRangeImpl(startPointer, endPointer);
  }

  public static TextRange merge(List<TextRange> ranges) {
    return new TextRangeImpl(
      ranges.stream().map(TextRange::start).min(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER),
      ranges.stream().map(TextRange::end).max(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER)
    );
  }
}
