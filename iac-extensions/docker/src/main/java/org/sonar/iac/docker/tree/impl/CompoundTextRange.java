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
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public class CompoundTextRange extends TextRange {
  private final List<TextRange> textRanges;

  public CompoundTextRange(List<TextRange> textRanges) {
    super(textRanges.get(0).start(), textRanges.get(textRanges.size() - 1).end());
    this.textRanges = textRanges;
  }

  /**
   * Provide the correct {@link TextRange}, which can extend a multiple lines.
   * It combines the {@code startIndex} and the {@code token} length to find which of the {@link #textRanges} are covered, to compute the correct single/multi line {@link TextRange}.
   */
  public TextRange computeTextRangeAtIndex(int startIndex, String token) {
    int i = 0;
    TextRange current = textRanges.get(i);
    TextRange startRange;
    // find the textRange for start index
    while (startIndex > current.end().lineOffset() - current.start().lineOffset()) {
      startIndex -= current.end().lineOffset() - current.start().lineOffset() + 1;
      i++;
      current = textRanges.get(i);
    }
    startRange = current;
    int end = startIndex + token.length();
    // continue to look for the end index
    while (end > current.end().lineOffset() - current.start().lineOffset()) {
      end -= current.end().lineOffset() - current.start().lineOffset() + 1;
      i++;
      current = textRanges.get(i);
    }
    // compute the final range
    int startLine = startRange.start().line();
    int startOffset = startRange.start().lineOffset() + startIndex;
    int endLine = current.end().line();
    int endOffset = (startRange.start().line() != current.end().line() ? 0 : startRange.start().lineOffset()) + end;
    return TextRanges.range(startLine, startOffset, endLine, endOffset);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    CompoundTextRange otherRange = (CompoundTextRange) other;
    return textRanges.equals(otherRange.textRanges);
  }

  @Override
  public int hashCode() {
    return textRanges.hashCode();
  }
}
