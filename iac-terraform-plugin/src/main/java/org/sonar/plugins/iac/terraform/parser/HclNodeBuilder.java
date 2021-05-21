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

import com.sonar.sslr.api.Rule;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.TokenType;
import com.sonar.sslr.api.Trivia;
import com.sonar.sslr.api.typed.Input;
import com.sonar.sslr.api.typed.NodeBuilder;
import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.iac.terraform.api.tree.TextRange;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxTrivia;
import org.sonar.plugins.iac.terraform.tree.impl.TerraformTree;
import org.sonar.plugins.iac.terraform.tree.impl.TextRanges;
import org.sonar.sslr.grammar.GrammarRuleKey;

public class HclNodeBuilder implements NodeBuilder {

  public static final char BYTE_ORDER_MARK = '\uFEFF';

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
    String value = input.substring(startIndex, endIndex);
    TextRange range = tokenRange(input, startIndex, value);
    return new InternalSyntaxToken(value, range, createTrivias(trivias));
  }

  private static List<SyntaxTrivia> createTrivias(List<Trivia> trivias) {
    List<SyntaxTrivia> result = new ArrayList<>();
    for (Trivia trivia : trivias) {
      Token trivialToken = trivia.getToken();
      String comment = trivialToken.getValue();
      TextRange range = TextRanges.range(trivialToken.getLine(), trivialToken.getColumn(), comment);
      result.add(new InternalSyntaxTrivia(comment, range));
    }
    return result;
  }

  private static TextRange tokenRange(Input input, int startIndex, String value) {
    int[] lineAndColumn = input.lineAndColumnAt(startIndex);
    char[] fileChars = input.input();
    boolean hasByteOrderMark = fileChars.length > 0 && fileChars[0] == BYTE_ORDER_MARK;
    int column = applyByteOrderMark(lineAndColumn[1], hasByteOrderMark) - 1;
    return TextRanges.range(lineAndColumn[0], column, value);
  }

  private static int applyByteOrderMark(int column, boolean hasByteOrderMark) {
    return hasByteOrderMark ? column - 1 : column;
  }

  private static class InternalSyntaxSpacing extends TerraformTree {

    @Override
    public List<Tree> children() {
      throw new UnsupportedOperationException();
    }

  }
}
