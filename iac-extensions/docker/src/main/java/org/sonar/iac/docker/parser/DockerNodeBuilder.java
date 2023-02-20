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
import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.CommentImpl;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.impl.AbstractDockerTreeImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class DockerNodeBuilder implements NodeBuilder {
  public static final char BYTE_ORDER_MARK = '\uFEFF';

  private DockerPreprocessor.SourceOffset sourceOffset;

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

  public void setSourceOffset(DockerPreprocessor.SourceOffset sourceOffset) {
    this.sourceOffset = sourceOffset;
  }
}
