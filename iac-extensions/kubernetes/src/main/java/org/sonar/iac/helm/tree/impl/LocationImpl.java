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
package org.sonar.iac.helm.tree.impl;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sonar.iac.common.api.tree.impl.TextPointer;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.helm.tree.api.Location;

public class LocationImpl implements Location {

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\z|\\r\\n|[" + NEW_LINE + "])");
  private static final String LINE_CONTENT_GROUP = "lineContent";
  private static final String NEW_LINE_GROUP = "newLine";

  private final int position;

  private final int length;

  public LocationImpl(int position, int length) {
    this.position = position;
    this.length = length;
  }

  @Override
  public int position() {
    return position;
  }

  @Override
  public int length() {
    return length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var location = (LocationImpl) o;
    return position == location.position && length == location.length;
  }

  @Override
  public int hashCode() {
    return Objects.hash(position, length);
  }

  @Override
  public String toString() {
    return "LocationImpl{" +
      "position=" + position +
      ", length=" + length +
      '}';
  }

  public static Location fromTextRange(TextRange textRange, String text) {
    var startOffset = toPosition(textRange.start(), text);
    var endOffset = toPosition(textRange.end(), text);

    return new LocationImpl(startOffset, endOffset - startOffset);
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
    var lineContent = matcher.group(LINE_CONTENT_GROUP);
    var newLine = matcher.group(NEW_LINE_GROUP);
    var newLineLength = newLine.length();

    if (textPointer.lineOffset() > lineContent.length() + newLineLength) {
      var message = String.format("Unable to calculate position from TextRange, line offset %s is too big", textPointer.lineOffset());
      throw new IllegalArgumentException(message);
    }
    return positionCounter + textPointer.lineOffset();
  }

  private static int sumLineLengthsUntilLine(int lineNumber, Matcher matcher) {
    var positionCounter = 0;
    for (var i = 1; i < lineNumber; i++) {
      if (matcher.find()) {
        var lineContent = matcher.group(LINE_CONTENT_GROUP);
        var newLine = matcher.group(NEW_LINE_GROUP);
        var newLineLength = newLine.length();
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

  public TextRange toTextRange(String sourceCode) {
    var start = toTextPointer(position, sourceCode);
    var end = toTextPointer(position + length, sourceCode);
    return new TextRange(start, end);
  }

  private static TextPointer toTextPointer(int position, String sourceCode) {
    var matcher = LINE_PATTERN.matcher(sourceCode);
    var lineCounter = 1;
    var positionCounter = 0;

    var lineContent = "";
    var newLine = "";
    var foundLineNumber = false;
    while (matcher.find()) {
      lineContent = matcher.group(LINE_CONTENT_GROUP);
      newLine = matcher.group(NEW_LINE_GROUP);
      if (lineContent.isEmpty() && newLine.isEmpty()) {
        break;
      }
      var newPositionCounter = positionCounter + lineContent.length() + newLine.length();
      if (newPositionCounter > position) {
        foundLineNumber = true;
        break;
      }
      if (newPositionCounter == position) {
        if (!newLine.isEmpty()) {
          lineCounter++;
          positionCounter = newPositionCounter;
        }
        foundLineNumber = true;
        break;
      }
      lineCounter++;
      positionCounter = newPositionCounter;
    }
    if (!foundLineNumber) {
      var message = String.format("The position %s is too big for text length %s", position, sourceCode.length());
      throw new IllegalArgumentException(message);
    }

    return new TextPointer(lineCounter, position - positionCounter);
  }
}
