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

import java.util.Objects;
import java.util.regex.Pattern;

public class TextRange {

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\z|\\r\\n|[" + NEW_LINE + "])");

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

  @Override
  public String toString() {
    return "[" + start.line() + ":" + start.lineOffset() + "/" + end.line() + ":" + end.lineOffset() + "]";
  }

  /**
   * If the {@link TextRange} is longer than the line length then the new {@link TextRange} is returned with the
   * adjustment to the end line length.
   * @param content the text on witch adjustment is calculated
   * @return new adjusted {@link TextRange}
   */
  public TextRange trimEndToText(String content) {
    var matcher = LINE_PATTERN.matcher(content);
    var lineCounter = 1;
    var foundEndLine = false;
    var previousNewLine = "";

    while (matcher.find()) {
      var lineContent = matcher.group("lineContent");
      var newLine = matcher.group("newLine");
      if (lineContent.isEmpty() && newLine.isEmpty()) {
        if (lineCounter == end.line() && !previousNewLine.isEmpty()) {
          // the content ends with new line, so it's ok to have a position after last new line character
          foundEndLine = true;
        }
        break;
      }
      if (lineCounter == end.line()) {
        foundEndLine = true;
        break;
      }
      lineCounter++;
      previousNewLine = newLine;
    }
    if (!foundEndLine) {
      var message = String.format("The code contains %s lines, but end text range line is %s", lineCounter, end().line());
      throw new IllegalArgumentException(message);
    }
    var lineContent = matcher.group("lineContent");
    if (end.lineOffset() >= lineContent.length()) {
      return new TextRange(start, new TextPointer(end().line(), lineContent.length()));
    }
    return this;
  }
}
