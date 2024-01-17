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

import java.util.List;
import java.util.regex.Pattern;

public final class LineNumberCommentInserter {

  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\r\\n|[" + NEW_LINE + "])");

  private static final List<String> LINES_IGNORE_LINE_COUNTER = List.of("---", "...");

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
    while (matcher.find()) {
      lineCounter++;
      var lineContent = matcher.group("lineContent");
      sb.append(lineContent);
      if (!LINES_IGNORE_LINE_COUNTER.contains(lineContent)) {
        sb.append(commentLineNumber(lineCounter));
      }
      sb.append(matcher.group("newLine"));
      lastIndex = matcher.end();
    }
    var lastLine = content.substring(lastIndex);
    sb.append(lastLine);
    if (!LINES_IGNORE_LINE_COUNTER.contains(lastLine)) {
      sb.append(commentLineNumber(lineCounter + 1));
    }
    return sb.toString();
  }

  private static String commentLineNumber(int number) {
    return " #" + number;
  }
}
