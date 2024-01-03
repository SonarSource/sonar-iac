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
package org.sonar.iac.terraform.parser.grammar;

import com.sonar.sslr.api.typed.GrammarBuilder;
import org.sonar.iac.common.parser.grammar.Punctuator;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.ForObjectTree;
import org.sonar.iac.terraform.api.tree.ForTupleTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.ParenthesizedExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.TemplateForDirectiveTree;
import org.sonar.iac.terraform.api.tree.TemplateIfDirectiveTree;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.parser.TreeFactory;
import org.sonar.iac.terraform.tree.impl.AbstractForTree;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.TemplateForDirectiveTreeImpl;
import org.sonar.iac.terraform.tree.impl.TemplateIfDirectiveTreeImpl;

public class HclGrammar {

  private final GrammarBuilder<SyntaxTokenImpl> b;
  private final TreeFactory f;

  public HclGrammar(GrammarBuilder<SyntaxTokenImpl> b, TreeFactory f) {
    this.b = b;
    this.f = f;
  }

  public FileTree FILE() {
    return b.<FileTree>nonterminal(HclLexicalGrammar.FILE).is(
      f.file(b.zeroOrMore(
        b.firstOf(ATTRIBUTE(), BLOCK(), ONE_LINE_BLOCK())),
        b.optional(b.token(HclLexicalGrammar.SPACING)),
        b.token(HclLexicalGrammar.EOF)));
  }

  public BlockTree BLOCK() {
    return b.<BlockTree>nonterminal(HclLexicalGrammar.BLOCK).is(
      f.block(b.token(HclLexicalGrammar.IDENTIFIER),
        b.zeroOrMore(LABEL()),
        b.token(Punctuator.LCURLYBRACE),
        b.token(HclLexicalGrammar.NEWLINE),
        b.zeroOrMore(b.firstOf(ATTRIBUTE(), BLOCK(), ONE_LINE_BLOCK())),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public BlockTree ONE_LINE_BLOCK() {
    return b.<BlockTree>nonterminal(HclLexicalGrammar.ONE_LINE_BLOCK).is(
      f.oneLineBlock(b.token(HclLexicalGrammar.IDENTIFIER),
        b.zeroOrMore(LABEL()),
        b.token(Punctuator.LCURLYBRACE),
        b.optional(ATTRIBUTE()),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public LabelTree LABEL() {
    return b.<LabelTree>nonterminal(HclLexicalGrammar.LABEL).is(
      f.label(b.firstOf(b.token(HclLexicalGrammar.STRING_LITERAL), b.token(HclLexicalGrammar.IDENTIFIER))));
  }

  public AttributeTree ATTRIBUTE() {
    return b.<AttributeTree>nonterminal(HclLexicalGrammar.ATTRIBUTE).is(
      f.attribute(b.token(HclLexicalGrammar.IDENTIFIER), b.token(Punctuator.EQU), EXPRESSION()));
  }

  public ExpressionTree EXPRESSION() {
    return b.<ExpressionTree>nonterminal(HclLexicalGrammar.EXPRESSION).is(CONDITIONAL_OR_EXPR());
  }

  public ExpressionTree CONDITIONAL_OR_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(CONDITIONAL_AND_EXPR(),
        b.zeroOrMore(f.newPair(b.token(Punctuator.OR), CONDITIONAL_AND_EXPR()))));
  }

  public ExpressionTree CONDITIONAL_AND_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(EQUALITY_EXPR(),
        b.zeroOrMore(f.newPair(b.token(Punctuator.AND), EQUALITY_EXPR()))));
  }

  public ExpressionTree EQUALITY_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(RELATIONAL_EXPR(),
        b.zeroOrMore(f.newPair(
          b.firstOf(b.token(Punctuator.EQUAL), b.token(Punctuator.NOT_EQUAL)),
          RELATIONAL_EXPR()))));
  }

  public ExpressionTree RELATIONAL_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(ADDITIVE_EXPR(),
        b.zeroOrMore(f.newPair(
          b.firstOf(b.token(Punctuator.GREATER_OR_EQUAL), b.token(Punctuator.GREATER_THAN), b.token(Punctuator.LESS_OR_EQUAL), b.token(Punctuator.LESS_THAN)),
          ADDITIVE_EXPR()))));
  }

  public ExpressionTree ADDITIVE_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(MULTIPLICATIVE_EXPR(),
        b.zeroOrMore(f.newPair(
          b.firstOf(b.token(Punctuator.PLUS), b.token(Punctuator.MINUS)),
          MULTIPLICATIVE_EXPR()))));
  }

  public ExpressionTree MULTIPLICATIVE_EXPR() {
    return b.<ExpressionTree>nonterminal().is(
      f.binaryExpression(PREFIX_EXPRESSION(),
        b.zeroOrMore(f.newPair(
          b.firstOf(b.token(Punctuator.STAR), b.token(Punctuator.DIV), b.token(Punctuator.PERCENT)),
          PREFIX_EXPRESSION()))));
  }

  public ExpressionTree PREFIX_EXPRESSION() {
    return b.<ExpressionTree>nonterminal().is(
      f.prefixExpression(b.zeroOrMore(b.firstOf(b.token(Punctuator.MINUS), b.token(Punctuator.EXCLAMATION))),
        f.expression(PRIMARY_EXPRESSION(), b.zeroOrMore(POSTFIX_EXPRESSION()))));
  }

  public ExpressionTree PRIMARY_EXPRESSION() {
    return b.<ExpressionTree>nonterminal().is(
      b.firstOf(LITERAL_EXPRESSION(),
        TUPLE(),
        OBJECT(),
        QUOTED_TEMPLATE(),
        FUNCTION_CALL(),
        VARIABLE_EXPRESSION(),
        FOR_TUPLE(),
        FOR_OBJECT(),
        PARENTHESIZED_EXPRESSION()));
  }

  public ExpressionTree QUOTED_TEMPLATE() {
    return b.<ExpressionTree>nonterminal(HclLexicalGrammar.QUOTED_TEMPLATE).is(
      b.firstOf(
        f.stringLiteral(b.token(HclLexicalGrammar.STRING_WITHOUT_INTERPOLATION)),
        f.templateExpr(
          b.token(HclLexicalGrammar.SPACING),
          b.token(Punctuator.DOUBLE_QUOTE),
          b.oneOrMore(
            b.firstOf(
              f.templateStringLiteral(b.token(HclLexicalGrammar.QUOTED_TEMPLATE_STRING_CHARACTERS)),
              TEMPLATE())),
          b.token(Punctuator.DOUBLE_QUOTE))));
  }

  public ExpressionTree TEMPLATE() {
    return b.<ExpressionTree>nonterminal().is(
      b.firstOf(TEMPLATE_INTERPOLATION(),
        TEMPLATE_IF_DIRECTIVE(),
        TEMPLATE_FOR_DIRECTIVE(),
        f.templateStringLiteral(b.token(HclLexicalGrammar.TEMPLATE_LITERAL))));
  }

  public TemplateInterpolationTree TEMPLATE_INTERPOLATION() {
    return b.<TemplateInterpolationTree>nonterminal().is(
      f.templateInterpolation(
        b.firstOf(b.token(Punctuator.DOLLAR_LCURLY_TILDE), b.token(Punctuator.DOLLAR_LCURLY)),
        EXPRESSION(),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE))));
  }

  public TemplateIfDirectiveTree TEMPLATE_IF_DIRECTIVE() {
    return b.<TemplateIfDirectiveTree>nonterminal().is(
      f.templateIfDirective(
        TEMPLATE_IF_DIRECTIVE_IF_PART(),
        b.optional(TEMPLATE_IF_DIRECTIVE_ELSE_PART()),
        b.firstOf(b.token(Punctuator.PERCENT_LCURLY_TILDE), b.token(Punctuator.PERCENT_LCURLY)),
        b.token(HclKeyword.END_IF),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE))));
  }

  public TemplateIfDirectiveTreeImpl.IfPart TEMPLATE_IF_DIRECTIVE_IF_PART() {
    return b.<TemplateIfDirectiveTreeImpl.IfPart>nonterminal().is(
      f.templateIfDirectiveIfPart(
        b.firstOf(b.token(Punctuator.PERCENT_LCURLY_TILDE), b.token(Punctuator.PERCENT_LCURLY)),
        b.token(HclKeyword.IF),
        EXPRESSION(),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE)),
        TEMPLATE()));
  }

  public TemplateIfDirectiveTreeImpl.ElsePart TEMPLATE_IF_DIRECTIVE_ELSE_PART() {
    return b.<TemplateIfDirectiveTreeImpl.ElsePart>nonterminal().is(
      f.templateIfDirectiveElsePart(
        b.firstOf(b.token(Punctuator.PERCENT_LCURLY_TILDE), b.token(Punctuator.PERCENT_LCURLY)),
        b.token(HclKeyword.ELSE),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE)),
        TEMPLATE()));
  }

  public TemplateForDirectiveTree TEMPLATE_FOR_DIRECTIVE() {
    return b.<TemplateForDirectiveTree>nonterminal().is(
      f.templateForDirective(
        TEMPLATE_FOR_DIRECTIVE_INTRO(),
        TEMPLATE(),
        b.firstOf(b.token(Punctuator.PERCENT_LCURLY_TILDE), b.token(Punctuator.PERCENT_LCURLY)),
        b.token(HclKeyword.END_FOR),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE))));
  }

  public TemplateForDirectiveTreeImpl.Intro TEMPLATE_FOR_DIRECTIVE_INTRO() {
    return b.<TemplateForDirectiveTreeImpl.Intro>nonterminal().is(
      f.templateForDirectiveIntro(
        b.firstOf(b.token(Punctuator.PERCENT_LCURLY_TILDE), b.token(Punctuator.PERCENT_LCURLY)),
        b.token(HclKeyword.FOR),
        f.forIntroIdentifiers(VARIABLE_EXPRESSION(), b.optional(f.newPair(b.token(Punctuator.COMMA), VARIABLE_EXPRESSION()))),
        b.token(HclKeyword.IN),
        EXPRESSION(),
        b.firstOf(b.token(Punctuator.TILDE_RCURLY), b.token(Punctuator.RCURLYBRACE))));
  }

  public ParenthesizedExpressionTree PARENTHESIZED_EXPRESSION() {
    return b.<ParenthesizedExpressionTree>nonterminal().is(
      f.parenthesizedExpression(b.token(Punctuator.LPARENTHESIS), EXPRESSION(), b.token(Punctuator.RPARENTHESIS)));
  }

  public TreeFactory.PartialAccess POSTFIX_EXPRESSION() {
    return b.<TreeFactory.PartialAccess>nonterminal().is(
      b.firstOf(INDEX_ACCESS(), ATTRIBUTE_ACCESS(), INDEX_SPLAT_ACCESS(), ATTRIBUTE_SPLAT_ACCESS(), CONDITION()));
  }

  public TreeFactory.PartialAccess CONDITION() {
    return b.<TreeFactory.PartialAccess>nonterminal().is(
      f.condition(b.token(Punctuator.QUERY), EXPRESSION(), b.token(Punctuator.COLON), EXPRESSION()));
  }

  public TreeFactory.PartialAttributeAccess ATTRIBUTE_ACCESS() {
    return b.<TreeFactory.PartialAttributeAccess>nonterminal().is(
      f.partialAttributeAccess(
        b.token(Punctuator.DOT),
        b.firstOf(b.token(HclLexicalGrammar.IDENTIFIER), b.token(HclLexicalGrammar.NUMERIC_INDEX))));
  }

  public TreeFactory.PartialIndexAccess INDEX_ACCESS() {
    return b.<TreeFactory.PartialIndexAccess>nonterminal().is(
      f.partialIndexAccess(
        b.token(Punctuator.LBRACKET),
        EXPRESSION(),
        b.token(Punctuator.RBRACKET)));
  }

  public TreeFactory.PartialIndexSplatAccess INDEX_SPLAT_ACCESS() {
    return b.<TreeFactory.PartialIndexSplatAccess>nonterminal().is(
      f.partialIndexSplatAccess(
        b.token(Punctuator.LBRACKET),
        b.token(Punctuator.STAR),
        b.token(Punctuator.RBRACKET)));
  }

  public TreeFactory.PartialAttrSplatAccess ATTRIBUTE_SPLAT_ACCESS() {
    return b.<TreeFactory.PartialAttrSplatAccess>nonterminal().is(
      f.partialAttrSplatAccess(
        b.token(Punctuator.DOT),
        b.token(Punctuator.STAR)));
  }

  public ObjectTree OBJECT() {
    return b.<ObjectTree>nonterminal(HclLexicalGrammar.OBJECT).is(
      f.object(b.token(Punctuator.LCURLYBRACE),
        b.optional(OBJECT_ELEMENTS()),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public ForTupleTree FOR_TUPLE() {
    return b.<ForTupleTree>nonterminal().is(
      f.forTuple(b.token(Punctuator.LBRACKET),
        FOR_INTRO(),
        EXPRESSION(),
        b.optional(f.newPair(b.token(HclKeyword.IF), EXPRESSION())),
        b.token(Punctuator.RBRACKET)));
  }

  public ForObjectTree FOR_OBJECT() {
    return b.<ForObjectTree>nonterminal().is(
      f.forObject(b.token(Punctuator.LCURLYBRACE),
        FOR_INTRO(),
        EXPRESSION(),
        b.token(Punctuator.DOUBLEARROW),
        EXPRESSION(),
        b.optional(b.token(Punctuator.ELLIPSIS)),
        b.optional(f.newPair(b.token(HclKeyword.IF), EXPRESSION())),
        b.token(Punctuator.RCURLYBRACE)));
  }

  public AbstractForTree.ForIntro FOR_INTRO() {
    return b.<AbstractForTree.ForIntro>nonterminal().is(
      f.forIntro(b.token(HclKeyword.FOR),
        f.forIntroIdentifiers(VARIABLE_EXPRESSION(), b.optional(f.newPair(b.token(Punctuator.COMMA), VARIABLE_EXPRESSION()))),
        b.token(HclKeyword.IN),
        EXPRESSION(),
        b.token(Punctuator.COLON)));
  }

  public SeparatedTrees<ObjectElementTree> OBJECT_ELEMENTS() {
    return b.<SeparatedTrees<ObjectElementTree>>nonterminal().is(
      f.objectElements(OBJECT_ELEMENT(),
        b.zeroOrMore(f.newPair(b.firstOf(b.token(Punctuator.COMMA), b.token(HclLexicalGrammar.NEWLINE)), OBJECT_ELEMENT())),
        b.optional(b.token(Punctuator.COMMA))));
  }

  public TupleTree TUPLE() {
    return b.<TupleTree>nonterminal(HclLexicalGrammar.TUPLE).is(
      f.tuple(b.token(Punctuator.LBRACKET),
        b.optional(TUPLE_ELEMENTS()),
        b.token(Punctuator.RBRACKET)));
  }

  public SeparatedTrees<ExpressionTree> TUPLE_ELEMENTS() {
    return b.<SeparatedTrees<ExpressionTree>>nonterminal().is(
      f.tupleElements(EXPRESSION(),
        b.zeroOrMore(f.newPair(b.token(Punctuator.COMMA), EXPRESSION())),
        b.optional(b.token(Punctuator.COMMA))));
  }

  public ObjectElementTree OBJECT_ELEMENT() {
    return b.<ObjectElementTree>nonterminal(HclLexicalGrammar.OBJECT_ELEMENT).is(
      f.objectElement(
        EXPRESSION(),
        b.firstOf(b.token(Punctuator.EQU), b.token(Punctuator.COLON)),
        EXPRESSION()));
  }

  public ExpressionTree LITERAL_EXPRESSION() {
    return b.<ExpressionTree>nonterminal(HclLexicalGrammar.LITERAL_EXPRESSION).is(
      b.firstOf(
        f.numericLiteral(b.token(HclLexicalGrammar.NUMERIC_LITERAL)),
        f.booleanLiteral(b.token(HclLexicalGrammar.BOOLEAN_LITERAL)),
        f.nullLiteral(b.token(HclLexicalGrammar.NULL)),
        f.heredocLiteral(b.token(HclLexicalGrammar.HEREDOC_LITERAL))));
  }

  public VariableExprTree VARIABLE_EXPRESSION() {
    return b.<VariableExprTree>nonterminal(HclLexicalGrammar.VARIABLE_EXPRESSION).is(
      f.variable(b.token(HclLexicalGrammar.IDENTIFIER)));
  }

  public FunctionCallTree FUNCTION_CALL() {
    return b.<FunctionCallTree>nonterminal(HclLexicalGrammar.FUNCTION_CALL).is(
      f.functionCall(b.token(HclLexicalGrammar.IDENTIFIER),
        b.token(Punctuator.LPARENTHESIS),
        b.optional(FUNCTION_CALL_ARGUMENTS()),
        b.token(Punctuator.RPARENTHESIS)));
  }

  public SeparatedTrees<ExpressionTree> FUNCTION_CALL_ARGUMENTS() {
    return b.<SeparatedTrees<ExpressionTree>>nonterminal().is(
      f.functionCallArguments(EXPRESSION(),
        b.zeroOrMore(f.newPair(b.token(Punctuator.COMMA), EXPRESSION())),
        b.optional(b.firstOf(b.token(Punctuator.COMMA), b.token(Punctuator.ELLIPSIS)))));
  }
}
