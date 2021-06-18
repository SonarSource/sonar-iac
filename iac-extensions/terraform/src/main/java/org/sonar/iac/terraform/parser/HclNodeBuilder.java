/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.parser;

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
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.TerraformTreeImpl;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class HclNodeBuilder implements NodeBuilder {

  public static final char BYTE_ORDER_MARK = '\uFEFF';

  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof SyntaxTokenImpl) {
        return child;
      }
    }

    return new TerraformTreeImpl() {
      @Override
      public List<Tree> children() {
        throw new UnsupportedOperationException();
      }

      @Override
      public TerraformTree.Kind getKind() {
        return TerraformTree.Kind.TOKEN;
      }
    };
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    String value = input.substring(startIndex, endIndex);
    TextRange range = tokenRange(input, startIndex, value);
    return new SyntaxTokenImpl(value, range, createComments(trivias));
  }

  private static TextRange tokenRange(Input input, int startIndex, String value) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    int column = applyByteOrderMark(lineAndColumn[1], hasByteOrderMark) - 1;
    return TextRanges.range(lineAndColumn[0], column, value);
  }

  private static int applyByteOrderMark(int column, boolean hasByteOrderMark) {
    return hasByteOrderMark ? (column - 1) : column;
  }

  private static List<Comment> createComments(List<Trivia> trivias) {
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
}
