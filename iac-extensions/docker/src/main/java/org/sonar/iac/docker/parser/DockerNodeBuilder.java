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
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.parser.DockerPreprocessor.PreprocessorResult;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.impl.AbstractDockerTreeImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class DockerNodeBuilder implements NodeBuilder {
  public static final char BYTE_ORDER_MARK = '\uFEFF';

  private DockerPreprocessor.SourceOffset sourceOffset;
  private Iterator<Map.Entry<Integer, Comment>> commentMapIterator;

  @Nullable
  private Map.Entry<Integer, Comment> nextComment;

  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof SyntaxTokenImpl) {
        return child;
      }
    }

    return new AbstractDockerTreeImpl() {
      @Override
      public List<Tree> children() {
        throw new UnsupportedOperationException();
      }

      @Override
      public DockerTree.Kind getKind() {
        return DockerTree.Kind.TOKEN;
      }
    };
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    String value = input.substring(startIndex, endIndex);
    TextRange range = tokenRange(input, startIndex, value);
    // TODO SONARIAC-533: create comment should be replaced with getCommentsForToken
    return new SyntaxTokenImpl(value, range, createComments(trivias));
  }

  private TextRange tokenRange(Input input, int startIndex, String value) {
    int[] startLineAndColumn = sourceOffset.sourceLineAndColumnAt(startIndex);
    int[] endLineAndColumn = sourceOffset.sourceLineAndColumnAt(startIndex + value.length());
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;

    int startColumn = applyByteOrderMark(startLineAndColumn[1], hasByteOrderMark);
    int endColum = applyByteOrderMark(endLineAndColumn[1], hasByteOrderMark);

    return TextRanges.range(startLineAndColumn[0], startColumn, endLineAndColumn[0], endColum);
  }

  private static int applyByteOrderMark(int column, boolean hasByteOrderMark) {
    return (hasByteOrderMark ? (column - 1) : column) - 1;
  }

  /**
   * Comments are removed form the code in the preprocessing to handle escaped line breaks and inline comments correctly.
   * The stored comments are restored from the {@link PreprocessorResult} and added to the SyntaxToken during the parsing.
   */
  List<Comment> getCommentsForToken(TextRange tokenRange) {
    List<Comment> comments = new ArrayList<>();
    while (nextComment != null && isAllocatableComment(nextComment.getKey(), tokenRange)) {
      comments.add(nextComment.getValue());
      nextComment = commentMapIterator.hasNext() ? commentMapIterator.next() : null;
    }
    return comments;
  }

  /**
   * A comment is allocatable to a token when:
   * a. the comment line is before the token start line
   * b. the comment line is before the token end line which means the comment is an inline comment
   */
  private static boolean isAllocatableComment(int commentLine, TextRange tokenRange) {
    return commentLine < tokenRange.end().line();
  }

  // TODO SONARIAC-533: Can be removed because it will be obsolete when comment processing is handled by the Preprocessor
  private static List<Comment> createComments(List<Trivia> trivias) {
    List<Comment> result = new ArrayList<>();
    for (Trivia trivia : trivias) {
      Token triviaToken = trivia.getToken();
      String text = triviaToken.getValue();
      TextRange range = TextRanges.range(triviaToken.getLine(), triviaToken.getColumn(), text);
      String contentText = text.length() > 1 ? text.substring(1).trim() : "";
      result.add(new CommentImpl(text, contentText, range));
    }
    return result;
  }

  public void setPreprocessorResult(PreprocessorResult preprocessorResult) {
    sourceOffset = preprocessorResult.sourceOffset();
    commentMapIterator = preprocessorResult.commentMap().entrySet().iterator();
    if (commentMapIterator.hasNext()) {
      nextComment = commentMapIterator.next();
    }
  }
}
