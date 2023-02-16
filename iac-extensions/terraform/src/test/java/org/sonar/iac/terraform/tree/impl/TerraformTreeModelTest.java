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
package org.sonar.iac.terraform.tree.impl;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.iac.common.api.tree.HasTextRange;
import org.sonar.iac.common.extension.visitors.TreeContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.parser.HclParser;
import org.sonar.sslr.grammar.GrammarRuleKey;

public abstract class TerraformTreeModelTest {
  protected ActionParser<TerraformTree> p;

  /**
   * Parse the given string and return the first descendant of the given kind.
   *
   * @param s the string to parse
   * @param rootRule the rule to start parsing from
   * @return the node found for the given kind, null if not found.
   */
  protected <T extends TerraformTree> T parse(String s, GrammarRuleKey rootRule) {
    p = new HclParser(rootRule);
    TerraformTree node = p.parse(s);
    checkFullFidelity(node, s.trim());

    return (T) node;
  }

  /**
   * Return the concatenation of all the given node tokens value.
   */
  protected static String expressionToString(TerraformTree node) {
    SourceBuilder writer = new SourceBuilder();
    return writer.build(node).trim();
  }

  private static void checkFullFidelity(TerraformTree tree, String inputString) {
    String resultString = expressionToString(tree);
    if (!inputString.equals(resultString)) {
      String message;
      if (inputString.startsWith(resultString)) {
        message = "Only beginning of the input string is parsed: " + resultString;
      } else {
        message = "Some tokens are lost. See result tree string: " + resultString;
      }
      throw new RecognitionException(0, message);
    }
  }

  static class SourceBuilder extends TreeVisitor<TreeContext> {

    private final StringBuilder stringBuilder = new StringBuilder();
    private int line = 1;
    private int column = 0;

    public String build(TerraformTree tree) {
      register(SyntaxToken.class, (ctx, token) -> {
        token.comments().forEach(comment -> appendText(comment, comment.value()));
        appendText(token, token.value());
      });
      scan(new TreeContext(), tree);
      return stringBuilder.toString();
    }

    private void appendText(HasTextRange token, String text) {
      insertMissingSpaceBefore(token.textRange().start());
      stringBuilder.append(text);
      String[] lines = text.split("\r\n|\n|\r", -1);
      if (lines.length > 1) {
        line += lines.length - 1;
        column = lines[lines.length - 1].length();
      } else {
        column += text.length();
      }
    }

    private void insertMissingSpaceBefore(TextPointer startPointer) {

      int linesToInsert = startPointer.line() - line;
      if (linesToInsert < 0) {
        throw new IllegalStateException("Illegal token line for " + startPointer.lineOffset());
      } else if (linesToInsert > 0) {
        for (int i = 0; i < linesToInsert; i++) {
          stringBuilder.append("\n");
          line++;
        }
        column = 0;
      }
      int spacesToInsert = startPointer.lineOffset() - column;
      for (int i = 0; i < spacesToInsert; i++) {
        stringBuilder.append(' ');
        column++;
      }
    }


  }
}
