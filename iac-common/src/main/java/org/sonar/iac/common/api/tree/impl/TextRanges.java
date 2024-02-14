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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonarsource.analyzer.commons.TokenLocation;

import static java.util.Comparator.naturalOrder;

public class TextRanges {

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\Z|\\r\\n|[" + NEW_LINE + "])");
  private static final Supplier<IllegalArgumentException> MERGE_EXCEPTION_SUPPLIER = () -> new IllegalArgumentException("Can't merge 0 ranges");

  private TextRanges() {
  }

  public static TextRange range(int startLine, int startColumn, int endLine, int endColumn) {
    return new TextRange(new TextPointer(startLine, startColumn), new TextPointer(endLine, endColumn));
  }

  public static TextRange range(int line, int column, String value) {
    TokenLocation location = new TokenLocation(line, column, value);
    TextPointer startPointer = new TextPointer(location.startLine(), location.startLineOffset());
    TextPointer endPointer = new TextPointer(location.endLine(), location.endLineOffset());
    return new TextRange(startPointer, endPointer);
  }

  public static TextRange merge(List<TextRange> ranges) {
    return new TextRange(
      ranges.stream().map(TextRange::start).min(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER),
      ranges.stream().map(TextRange::end).max(naturalOrder()).orElseThrow(MERGE_EXCEPTION_SUPPLIER));
  }

  public static TextRange mergeElementsWithTextRange(List<? extends HasTextRange> elementsWithTextRange) {
    List<TextRange> textRanges = new ArrayList<>();
    for (HasTextRange element : elementsWithTextRange) {
      textRanges.add(element.textRange());
    }
    return TextRanges.merge(textRanges);
  }

  public static boolean isValidAndNotEmpty(TextRange range) {
    return range.end().compareTo(range.start()) > 0;
  }

  public static Tuple<Integer, Integer> toPositionAndLength(TextRange textRange, String text) {
    var positionCounter = toPosition(textRange.start(), text);
    var endPosition = toPosition(textRange.end(), text);

    return new Tuple<>(positionCounter, endPosition - positionCounter);
  }

  private static int toPosition(TextPointer textPointer, String text) {
    var matcher = LINE_PATTERN.matcher(text);
    var positionCounter = 0;

    positionCounter = sumLineLengthsUntilLine(textPointer.line(), matcher);
    if (positionCounter == text.length()) {
      var message = String.format("Unable to calculate position from TextRange, line %s doesn't exist", textPointer.line());
      throw new IllegalArgumentException(message);
    }
    moveToNextLine(matcher);
    var lineContent = matcher.group("lineContent");
    var newLine = matcher.group("newLine");
    var newLineLength = newLine != null ? newLine.length() : 0;

    if (textPointer.lineOffset() > lineContent.length() + newLineLength) {
      var message = String.format("Unable to calculate position from TextRange, line offset %s is too big", textPointer.line());
      throw new IllegalArgumentException(message);
    }
    return positionCounter + textPointer.lineOffset();
  }

  private static int sumLineLengthsUntilLine(int lineNumber, Matcher matcher) {
    int positionCounter = 0;
    for (int i = 1; i < lineNumber; i++) {
      if (matcher.find()) {
        var lineContent = matcher.group("lineContent");
        var newLine = matcher.group("newLine");
        var newLineLength = newLine != null ? newLine.length() : 0;
        positionCounter = positionCounter + lineContent.length() + newLineLength;
      } else {
        var message = String.format("Unable to calculate position from TextRange, line number %s is too big", lineNumber);
        throw new IllegalArgumentException(message);
      }
    }
    return positionCounter;
  }

  private static void moveToNextLine(Matcher matcher) {
    if (!matcher.find()) {
      throw new IllegalArgumentException("Unable to calculate position from TextRange, no next line");
    }
  }
}
