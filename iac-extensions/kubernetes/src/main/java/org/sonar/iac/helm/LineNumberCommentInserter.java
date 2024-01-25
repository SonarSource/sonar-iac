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
package org.sonar.iac.helm;

import java.util.regex.Pattern;

public final class LineNumberCommentInserter {

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\r\\n|[" + NEW_LINE + "])");

  private static final int DOUBLE_BRACE_SIZE = 2;

  private LineNumberCommentInserter() {
  }

  /**
   * For a given file content, it will process it line by line, and add a comment with a line number at the end of each of them. ('#5' for line five)
   * This is mean for Helm processing, to be able to track back the original source from the generated content.
   */
  public static String addLineComments(String content) {
    var sb = new StringBuilder();
    var lineCounter = 0;
    var matcher = LINE_PATTERN.matcher(content);

    var lastIndex = 0;
    var goTemplateStartLine = -1;
    while (matcher.find()) {
      // Iterate over all lines. Possible cases:
      // - Line contains {{ and corresponding }} on the same line: add line number comment
      // - Line contains regular text and is _not inside_ a go template: add line number comment
      // - Line contains regular text and is inside a go template: do not add line number comment
      // - Line contains {{ but no corresponding }} on the same line: do not add line number comment, store line number
      // - Line contains }} but no corresponding {{ on the same line: add line number comment, reset line number

      lineCounter++;
      var lineContent = matcher.group("lineContent");
      sb.append(lineContent);

      if (goTemplateStartLine == -1 && getNumberOfUnmatchedDoubleOpeningBraces(lineContent) == 0) {
        sb.append(commentLineNumber(lineCounter));
      } else if (goTemplateStartLine == -1 && getNumberOfUnmatchedDoubleOpeningBraces(lineContent) > 0) {
        goTemplateStartLine = lineCounter;
      } else if (getNumberOfUnmatchedDoubleOpeningBraces(lineContent) < 0) {
        sb.append(commentMultilineNumber(goTemplateStartLine, lineCounter));
        goTemplateStartLine = -1;
      }
      sb.append(matcher.group("newLine"));
      lastIndex = matcher.end();
    }
    var lastLine = content.substring(lastIndex);
    sb.append(lastLine);
    if (goTemplateStartLine != -1) {
      sb.append(commentMultilineNumber(goTemplateStartLine, lineCounter + 1));
    } else {
      sb.append(commentLineNumber(lineCounter + 1));
    }

    return sb.toString();
  }

  static int getNumberOfUnmatchedDoubleOpeningBraces(String line) {
    var count = 0;
    var index = 0;
    while (index < line.length() - 1) {
      if ("{{".equals(line.substring(index, index + DOUBLE_BRACE_SIZE))) {
        count++;
        index += DOUBLE_BRACE_SIZE;
      } else if ("}}".equals(line.substring(index, index + DOUBLE_BRACE_SIZE))) {
        count--;
        index += DOUBLE_BRACE_SIZE;
      } else {
        index++;
      }
    }
    return count;
  }

  private static String commentLineNumber(int number) {
    return " #" + number;
  }

  private static String commentMultilineNumber(int startLine, int endLine) {
    return " #" + startLine + ":" + endLine;
  }
}
