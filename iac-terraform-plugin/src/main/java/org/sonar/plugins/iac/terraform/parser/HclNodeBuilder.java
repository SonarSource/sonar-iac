/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.parser;

import com.sonar.sslr.api.GenericTokenType;
import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxTrivia;
import org.sonar.plugins.iac.terraform.tree.impl.TerraformTree;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class HclNodeBuilder implements NodeBuilder {

  public static final char BYTE_ORDER_MARK = '\uFEFF';
  private final int lineOffset;

  public HclNodeBuilder(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  public HclNodeBuilder() {
    this.lineOffset = 0;
  }

  @Override
  public Object createNonTerminal(GrammarRuleKey ruleKey, Rule rule, List<Object> children, int startIndex, int endIndex) {
    for (Object child : children) {
      if (child instanceof InternalSyntaxToken) {
        return child;
      }
    }

    // TODO: Do we need this ?
    return new InternalSyntaxSpacing();
  }

  @Override
  public Object createTerminal(Input input, int startIndex, int endIndex, List<Trivia> trivias, TokenType type) {
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    boolean isEof = GenericTokenType.EOF.equals(type);
    LineColumnValue lineColumnValue = tokenPosition(input, startIndex, endIndex);
    return new InternalSyntaxToken(
      lineColumnValue.line + lineOffset,
      column(hasByteOrderMark, lineColumnValue.line, lineColumnValue.column),
      lineColumnValue.value,
      createTrivias(trivias, hasByteOrderMark),
      startIndex - (hasByteOrderMark ? 1 : 0),
      isEof);
  }

  private static int column(boolean hasByteOrderMark, int line, int column) {
    if (hasByteOrderMark && line == 1) {
      return column - 1;
    }
    return column;
  }

  private static List<SyntaxTrivia> createTrivias(List<Trivia> trivias, boolean hasByteOrderMark) {
    List<SyntaxTrivia> result = new ArrayList<>();
    for (Trivia trivia : trivias) {
      Token trivialToken = trivia.getToken();
      int column = column(hasByteOrderMark, trivialToken.getLine(), trivialToken.getColumn());
      result.add(InternalSyntaxTrivia.create(trivialToken.getValue(), trivialToken.getLine(), column));
    }
    return result;
  }

  private static LineColumnValue tokenPosition(Input input, int startIndex, int endIndex) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    String value = input.substring(startIndex, endIndex);
    return new LineColumnValue(lineAndColumn[0], lineAndColumn[1] - 1, value);
  }

  private static class LineColumnValue {
    final int line;
    final int column;
    final String value;

    private LineColumnValue(int line, int column, String value) {
      this.line = line;
      this.column = column;
      this.value = value;
    }
  }

  private static class InternalSyntaxSpacing extends TerraformTree {

    @Override
    public List<Tree> children() {
      throw new UnsupportedOperationException();
    }

  }
}
