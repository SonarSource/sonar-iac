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

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.visitors.LocationShifter;

public final class LineNumberCommentRemover {
  private static final String NEW_LINE = "\\n\\r\\u2028\\u2029";
  private static final Set<Character> NEW_LINES_CHARACTERS = Set.of('\n', '\r', '\u2028', '\u2029');

  private static final Pattern LINE_PATTERN = Pattern.compile("(?<lineContent>[^" + NEW_LINE + "]*+)(?<newLine>\\r\\n|[" + NEW_LINE + "])");

  private static final Pattern CONTAINS_LINE_NUMBER_OR_RANGE = Pattern.compile("#(?<rangeStart>\\d++)(:(?<rangeEnd>\\d++))?( #\\d++:?\\d*+)*+$");

  private LineNumberCommentRemover() {
  }

  /**
   * This method removes all comments that contains line numbers and store them in {@link LocationShifter}.
   * Also blank lines that contains only trailing line comment number are removed.
   * Such lines may be produced after evaluation of Helm template.
   * In some cases such lines may cause parsing issues in snakeyaml-engine.
   */
  public static String cleanSource(String source, InputFileContext inputFileContext, LocationShifter locationShifter) {
    var sb = new StringBuilder();
    var matcher = LINE_PATTERN.matcher(source);

    var lastIndex = 0;
    var lineCounter = 1;
    while (matcher.find()) {
      var lineContent = matcher.group("lineContent");
      var lineAndComment = toLineAndComment(lineContent);
      if (!lineAndComment.contentWithoutComment.isBlank()) {
        lineAndComment.addToLocationShifter(locationShifter, inputFileContext, lineCounter);
        sb.append(lineAndComment.contentWithoutComment);
        sb.append(matcher.group("newLine"));
        lastIndex = matcher.end();
        lineCounter++;
      }
    }
    lineCounter++;
    var lastLine = source.substring(lastIndex);
    var lineAndComment = toLineAndComment(lastLine);
    if (!lineAndComment.contentWithoutComment.isBlank()) {
      sb.append(lineAndComment.contentWithoutComment);
      lineAndComment.addToLocationShifter(locationShifter, inputFileContext, lineCounter);
    }
    return removeNewLinesAtTheEnd(sb.toString());
  }

  private static String removeNewLinesAtTheEnd(String input) {
    int index = input.length() - 1;
    for (; index >= 0; index--) {
      if (!NEW_LINES_CHARACTERS.contains(input.charAt(index))) {
        break;
      }
    }
    return input.substring(0, index + 1);
  }

  private static LineAndComment toLineAndComment(String lineContent) {
    var commentMatcher = CONTAINS_LINE_NUMBER_OR_RANGE.matcher(lineContent);
    if (commentMatcher.find()) {
      var comment = commentMatcher.group();
      var endIndex = lineContent.indexOf(comment);
      var lineContentWithoutComment = lineContent.substring(0, Math.max(endIndex - 1, 0));
      var lineCommentRangeStart = Integer.parseInt(commentMatcher.group("rangeStart"));
      var lineCommentRangeEnd = Optional.ofNullable(commentMatcher.group("rangeEnd"))
        .map(Integer::parseInt)
        .orElse(lineCommentRangeStart);
      return new LineAndComment(lineContentWithoutComment, lineCommentRangeStart, lineCommentRangeEnd);
    }
    return new LineAndComment(lineContent);
  }

  private static class LineAndComment {
    private final String contentWithoutComment;
    private final Integer lineCommentRangeStart;
    private final Integer lineCommentRangeEnd;

    public LineAndComment(String contentWithoutComment) {
      this.contentWithoutComment = contentWithoutComment;
      lineCommentRangeStart = null;
      lineCommentRangeEnd = null;
    }

    public LineAndComment(String contentWithoutComment, Integer lineCommentRangeStart, Integer lineCommentRangeEnd) {
      this.contentWithoutComment = contentWithoutComment;
      this.lineCommentRangeStart = lineCommentRangeStart;
      this.lineCommentRangeEnd = lineCommentRangeEnd;
    }

    public void addToLocationShifter(LocationShifter locationShifter, InputFileContext inputFileContext, int lineCounter) {
      if (lineCommentRangeStart != null && lineCommentRangeEnd != null) {
        locationShifter.addShiftedLine(inputFileContext, lineCounter, lineCommentRangeStart, lineCommentRangeEnd);
      }
    }
  }
}
