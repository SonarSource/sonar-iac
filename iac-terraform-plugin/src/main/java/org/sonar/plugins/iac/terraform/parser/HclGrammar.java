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

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.plugins.iac.terraform.api.tree.AttributeTree;
import org.sonar.plugins.iac.terraform.api.tree.BlockTree;
import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.FileTree;
import org.sonar.plugins.iac.terraform.api.tree.LabelTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.api.tree.SeparatedList;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;

public class HclGrammar {

  private final GrammarBuilder<InternalSyntaxToken> b;
  private final TreeFactory f;

  public HclGrammar(GrammarBuilder<InternalSyntaxToken> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public FileTree FILE() {
    return b.<FileTree>nonterminal(HclLexicalGrammar.FILE).is(
      f.file(b.optional(BODY()), b.optional(b.token(HclLexicalGrammar.SPACING)), b.token(HclLexicalGrammar.EOF)));
  }

  public BodyTree BODY() {
    return b.<BodyTree>nonterminal(HclLexicalGrammar.BODY).is(
      f.body(b.oneOrMore(b.firstOf(ATTRIBUTE(), BLOCK(), ONE_LINE_BLOCK()))));
  }

  public BlockTree BLOCK() {
    return b.<BlockTree>nonterminal(HclLexicalGrammar.BLOCK).is(
      f.block(b.token(HclLexicalGrammar.IDENTIFIER),
        b.zeroOrMore(LABEL()),
        b.token(HclPunctuator.LCURLYBRACE),
        b.token(HclLexicalGrammar.NEWLINE),
        b.optional(BODY()),
        b.token(HclPunctuator.RCURLYBRACE)
      ));
  }

  public OneLineBlockTree ONE_LINE_BLOCK() {
    return b.<OneLineBlockTree>nonterminal(HclLexicalGrammar.ONE_LINE_BLOCK).is(
      f.oneLineBlock(b.token(HclLexicalGrammar.IDENTIFIER),
        b.zeroOrMore(LABEL()),
        b.token(HclPunctuator.LCURLYBRACE),
        b.optional(ATTRIBUTE()),
        b.token(HclPunctuator.RCURLYBRACE)
      ));
  }

  public LabelTree LABEL() {
    return b.<LabelTree>nonterminal(HclLexicalGrammar.LABEL).is(
      f.label(b.firstOf(b.token(HclLexicalGrammar.STRING_LITERAL), b.token(HclLexicalGrammar.IDENTIFIER)))
    );
  }

  public AttributeTree ATTRIBUTE() {
    return b.<AttributeTree>nonterminal(HclLexicalGrammar.ATTRIBUTE).is(
      f.attribute(b.token(HclLexicalGrammar.IDENTIFIER), b.token(HclPunctuator.EQU), EXPRESSION())
    );
  }

  public ExpressionTree EXPRESSION() {
    return b.<ExpressionTree>nonterminal(HclLexicalGrammar.EXPRESSION).is(
      b.firstOf(LITERAL_EXPRESSION(),
        OBJECT())
    );
  }

  public ObjectTree OBJECT() {
    return b.<ObjectTree>nonterminal(HclLexicalGrammar.OBJECT).is(
      f.object(b.token(HclPunctuator.LCURLYBRACE),
        b.optional(OBJECT_ELEMENTS()),
        b.token(HclPunctuator.RCURLYBRACE)));
  }

  public SeparatedList<ObjectElementTree> OBJECT_ELEMENTS() {
    return b.<SeparatedList<ObjectElementTree>>nonterminal().is(
      f.objectElements(OBJECT_ELEMENT(),
        b.zeroOrMore(f.newTuple(b.firstOf(b.token(HclPunctuator.COMMA), b.token(HclLexicalGrammar.NEWLINE)), OBJECT_ELEMENT())),
        b.optional(b.token(HclPunctuator.COMMA))
        ));
  }

  public ObjectElementTree OBJECT_ELEMENT() {
    return b.<ObjectElementTree>nonterminal(HclLexicalGrammar.OBJECT_ELEMENT).is(
      f.objectElement(
        b.firstOf(b.token(HclLexicalGrammar.IDENTIFIER), EXPRESSION()),
        b.firstOf(b.token(HclPunctuator.EQU), b.token(HclPunctuator.COLON)),
        EXPRESSION()));
  }

  public ExpressionTree LITERAL_EXPRESSION() {
    //TODO: in the HCL grammar strings are not part of literals but of TemplateExpr. Do we need this?
    return b.<ExpressionTree>nonterminal(HclLexicalGrammar.LITERAL_EXPRESSION).is(
      f.literalExpr(b.firstOf(
        b.token(HclLexicalGrammar.NUMERIC_LITERAL),
        b.token(HclLexicalGrammar.BOOLEAN_LITERAL),
        b.token(HclLexicalGrammar.NULL),
        b.token(HclLexicalGrammar.STRING_LITERAL))));
  }

}
