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

import java.util.Iterator;
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
    var rangeIterator = new IteratorWrapper<>(textRanges.iterator());
    rangeIterator.next();

    // find starting range
    int index = navigateToRangeAtIndex(rangeIterator, startIndex);
    TextRange startRange = rangeIterator.current();
    // find ending range
    int endIndex = index + token.length();
    int finalIndex = navigateToRangeAtIndex(rangeIterator, endIndex);
    TextRange endRange = rangeIterator.current();

    // compute the final range
    int startLine = startRange.start().line();
    int startOffset = startRange.start().lineOffset() + index;
    int endLine = endRange.end().line();
    int endOffset = finalIndex;
    if (startRange.start().line() == endRange.end().line()) {
      endOffset += startRange.start().lineOffset();
    }

    return TextRanges.range(startLine, startOffset, endLine, endOffset);
  }

  public int navigateToRangeAtIndex(IteratorWrapper<TextRange> iterator, int index) {
    TextRange currentRange = iterator.current();
    while (index > sizeOfRange(currentRange)) {
      index -= sizeOfRange(currentRange) + 1;
      currentRange = iterator.next();
    }
    return index;
  }

  private static int sizeOfRange(TextRange range) {
    return range.end().lineOffset() - range.start().lineOffset();
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

  static class IteratorWrapper<E> {
    private final Iterator<E> iterator;
    private E current;

    public IteratorWrapper(Iterator<E> iterator) {
      this.iterator = iterator;
    }

    public E next() {
      current = iterator.next();
      return current;
    }

    public E current() {
      return current;
    }
  }
}
