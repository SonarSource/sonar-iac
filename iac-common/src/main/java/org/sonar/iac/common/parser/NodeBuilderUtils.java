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
package org.sonar.iac.common.parser;

import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;

public class NodeBuilderUtils {

  private NodeBuilderUtils() {
  }

  public static final char BYTE_ORDER_MARK = '\uFEFF';

  public static List<Comment> createComments(List<Trivia> trivias) {
    List<Comment> result = new ArrayList<>();
    for (Trivia trivia : trivias) {
      Token triviaToken = trivia.getToken();
      String text = triviaToken.getValue();
      TextRange range = TextRanges.range(triviaToken.getLine(), triviaToken.getColumn(), text);
      result.add(new CommentImpl(text, getCommentContent(text), range));
    }
    return result;
  }

  private static String getCommentContent(String comment) {
    if (comment.startsWith("//")) {
      return comment.substring(2);
    } else if (comment.startsWith("#")) {
      return comment.substring(1);
    } else {
      return comment.substring(2, comment.length() - 2);
    }
  }

  public static TextRange tokenRange(Input input, int startIndex, String value) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    int column = applyByteOrderMark(lineAndColumn[1], hasByteOrderMark) - 1;
    return TextRanges.range(lineAndColumn[0], column, value);
  }

  private static int applyByteOrderMark(int column, boolean hasByteOrderMark) {
    return hasByteOrderMark ? (column - 1) : column;
  }
}
