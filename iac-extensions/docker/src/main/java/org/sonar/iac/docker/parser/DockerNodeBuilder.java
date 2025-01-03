/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.docker.parser;

import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.parser.DockerPreprocessor.PreprocessorResult;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;
import org.sonar.iac.docker.tree.impl.AbstractDockerTreeImpl;
import org.sonar.iac.docker.tree.impl.CompoundTextRange;
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
    return new SyntaxTokenImpl(value, range, getCommentsForToken(range));
  }

  /**
   * Compute the {@link TextRange} from a value at the given {@code startIndex}, regarding the provided {@code input} (source code).
   * In case it extend to multiple lines, we build a {@link CompoundTextRange} object with a reference to each line {@link TextRange}.
   * It is used to track back tokens in HereDoc, as currently the full HereDoc content of an instruction is considered as a single {@link SyntaxToken}
   * that extend to multiple lines, which is then parsed by a specific HereDoc parser. This is this parser that call this {@link #tokenRange(Input, int, String)}
   * method to split this big {@link SyntaxToken} into multiple {@link SyntaxToken}, each with its own range.
   * <br />
   * The {@code startIndex} is the position on the instruction line where the provided value begin.
   * In the line {@code RUN <<EOT cmd ...}, the {@code startIndex} would be 4 (to skip the {@code RUN } part) and the value would be the HereDoc
   * content: {@code <<EOT cmd ...}
   */
  protected TextRange tokenRange(Input input, int startIndex, String value) {
    List<TextRange> ranges = new ArrayList<>();
    int[] currentLineAndColumn = sourceOffset.sourceLineAndColumnAt(startIndex);
    int[] previousLineAndColumn = currentLineAndColumn;
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;

    int index;
    for (index = startIndex + 1; index < startIndex + value.length(); index++) {
      int[] startLineAndColumn = sourceOffset.sourceLineAndColumnAt(index);
      var startLine = startLineAndColumn[0];
      var currentLine = currentLineAndColumn[0];
      var currentColumn = currentLineAndColumn[1];
      var previousLine = previousLineAndColumn[0];
      var previousColumn = previousLineAndColumn[1];
      // detect line changes
      if (currentLine != startLine) {
        int startColumn = applyByteOrderMark(currentColumn, hasByteOrderMark);
        int endColum = applyByteOrderMark(previousColumn, hasByteOrderMark);
        ranges.add(TextRanges.range(currentLine, startColumn, previousLine, endColum));
        currentLineAndColumn = startLineAndColumn;
      }
      previousLineAndColumn = startLineAndColumn;
    }

    // Add remaining range in the list
    int finalIndex = index;
    // fix in case we want the range of an empty value
    if (value.isEmpty()) {
      finalIndex--;
    }
    int[] endLineAndColumn = sourceOffset.sourceLineAndColumnAt(finalIndex);
    int startColumn = applyByteOrderMark(currentLineAndColumn[1], hasByteOrderMark);
    int endColum = applyByteOrderMark(endLineAndColumn[1], hasByteOrderMark);
    ranges.add(TextRanges.range(currentLineAndColumn[0], startColumn, endLineAndColumn[0], endColum));

    if (ranges.size() == 1) {
      return ranges.get(0);
    } else {
      return new CompoundTextRange(ranges);
    }
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

  public void setPreprocessorResult(PreprocessorResult preprocessorResult) {
    sourceOffset = preprocessorResult.sourceOffset();
    commentMapIterator = preprocessorResult.commentMap().entrySet().iterator();
    nextComment = null;
    if (commentMapIterator.hasNext()) {
      nextComment = commentMapIterator.next();
    }
  }
}
