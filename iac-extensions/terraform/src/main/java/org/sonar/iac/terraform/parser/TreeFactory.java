/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.parser;

import com.sonar.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeSplatAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.ForObjectTree;
import org.sonar.iac.terraform.api.tree.ForTupleTree;
import org.sonar.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.ParenthesizedExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TemplateForDirectiveTree;
import org.sonar.iac.terraform.api.tree.TemplateIfDirectiveTree;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.api.tree.TupleTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;
import org.sonar.iac.terraform.tree.impl.AbstractForTree;
import org.sonar.iac.terraform.tree.impl.AttributeAccessTreeImpl;
import org.sonar.iac.terraform.tree.impl.AttributeSplatAccessTreeImpl;
import org.sonar.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.iac.terraform.tree.impl.BinaryExpressionTreeImpl;
import org.sonar.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.iac.terraform.tree.impl.BodyTreeImpl;
import org.sonar.iac.terraform.tree.impl.ConditionTreeImpl;
import org.sonar.iac.terraform.tree.impl.FileTreeImpl;
import org.sonar.iac.terraform.tree.impl.ForObjectTreeImpl;
import org.sonar.iac.terraform.tree.impl.ForTupleTreeImpl;
import org.sonar.iac.terraform.tree.impl.FunctionCallTreeImpl;
import org.sonar.iac.terraform.tree.impl.IndexAccessExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.IndexSplatAccessTreeImpl;
import org.sonar.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.iac.terraform.tree.impl.ObjectElementTreeImpl;
import org.sonar.iac.terraform.tree.impl.ObjectTreeImpl;
import org.sonar.iac.terraform.tree.impl.ParenthesizedExpressionTreeImpl;
import org.sonar.iac.terraform.tree.impl.PrefixExpressionTreeImpl;
import org.sonar.iac.terraform.tree.impl.SeparatedTreesImpl;
import org.sonar.iac.terraform.tree.impl.SyntaxTokenImpl;
import org.sonar.iac.terraform.tree.impl.TemplateExpressionTreeImpl;
import org.sonar.iac.terraform.tree.impl.TemplateForDirectiveTreeImpl;
import org.sonar.iac.terraform.tree.impl.TemplateIfDirectiveTreeImpl;
import org.sonar.iac.terraform.tree.impl.TemplateInterpolationTreeImpl;
import org.sonar.iac.terraform.tree.impl.TupleTreeImpl;
import org.sonar.iac.terraform.tree.impl.VariableExprTreeImpl;

public class TreeFactory {
  public FileTree file(Optional<List<StatementTree>> statements, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(statements.or(Collections.emptyList()), eof);
  }

  public BlockTree block(
    Optional<SyntaxToken> dynamic,
    SyntaxToken type,
    Optional<List<LabelTree>> labels,
    SyntaxToken openBrace,
    SyntaxToken newline,
    Optional<List<StatementTree>> statements,
    SyntaxToken closeBrace) {
    BodyTree body = new BodyTreeImpl(openBrace, newline, statements.or(Collections.emptyList()), closeBrace);
    return new BlockTreeImpl(dynamic.orNull(), type, labels.orNull(), body, Kind.BLOCK);
  }

  public BlockTree oneLineBlock(
    Optional<SyntaxToken> dynamic,
    SyntaxToken type,
    Optional<List<LabelTree>> labels,
    SyntaxToken openBrace,
    Optional<AttributeTree> attribute,
    SyntaxToken closeBrace) {
    List<StatementTree> statements = attribute.isPresent() ? Collections.singletonList(attribute.get()) : Collections.emptyList();
    BodyTree body = new BodyTreeImpl(openBrace, null, statements, closeBrace);
    return new BlockTreeImpl(dynamic.orNull(), type, labels.orNull(), body, Kind.ONE_LINE_BLOCK);
  }

  public LabelTree label(SyntaxToken token) {
    return new LabelTreeImpl(token);
  }

  public LiteralExprTreeImpl numericLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.NUMERIC_LITERAL, token);
  }

  public LiteralExprTreeImpl booleanLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.BOOLEAN_LITERAL, token);
  }

  public LiteralExprTreeImpl nullLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.NULL_LITERAL, token);
  }

  public LiteralExprTreeImpl stringLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.STRING_LITERAL, token);
  }

  public LiteralExprTree templateStringLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.TEMPLATE_STRING_PART_LITERAL, token);
  }

  public LiteralExprTreeImpl heredocLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Kind.HEREDOC_LITERAL, token);
  }

  public AttributeTree attribute(SyntaxToken key, SyntaxToken equalSign, ExpressionTree value) {
    return new AttributeTreeImpl(key, equalSign, value);
  }

  public ObjectTree object(SyntaxToken openBrace, Optional<SeparatedTrees<ObjectElementTree>> elements, SyntaxToken closeBrace) {
    return new ObjectTreeImpl(openBrace, elements.orNull(), closeBrace);
  }

  public ObjectElementTree objectElement(ExpressionTree key, SyntaxToken equalOrColonSign, ExpressionTree value) {
    return new ObjectElementTreeImpl(key, equalOrColonSign, value);
  }

  public SeparatedTrees<ObjectElementTree> objectElements(
    ObjectElementTree firstElement,
    Optional<List<Pair<SyntaxToken, ObjectElementTree>>> otherElements,
    Optional<SyntaxTokenImpl> trailingComma) {
    return separatedTrees(firstElement, otherElements, trailingComma.orNull());
  }

  public PartialAttributeAccess partialAttributeAccess(SyntaxToken accessToken, SyntaxToken attribute) {
    return new PartialAttributeAccess(accessToken, attribute);
  }

  public VariableExprTree variable(SyntaxTokenImpl token) {
    return new VariableExprTreeImpl(token);
  }

  public TupleTree tuple(SyntaxToken openbracket, Optional<SeparatedTrees<ExpressionTree>> elements, SyntaxToken closeBracket) {
    return new TupleTreeImpl(openbracket, elements.orNull(), closeBracket);
  }

  public SeparatedTrees<ExpressionTree> tupleElements(
    ExpressionTree firstElement,
    Optional<List<Pair<SyntaxToken, ExpressionTree>>> otherElements,
    Optional<SyntaxToken> trailingComma) {
    return separatedTrees(firstElement, otherElements, trailingComma.orNull());
  }

  public PartialIndexAccess partialIndexAccess(SyntaxToken openBracket, ExpressionTree subject, SyntaxToken closeBracket) {
    return new PartialIndexAccess(openBracket, subject, closeBracket);
  }

  public ExpressionTree expression(ExpressionTree primary, Optional<List<PartialAccess>> optionalAccesses) {
    if (!optionalAccesses.isPresent()) {
      return primary;
    }

    List<PartialAccess> accesses = optionalAccesses.get();

    ExpressionTree result = accesses.get(0).complete(primary);
    for (PartialAccess attribute : accesses.subList(1, accesses.size())) {
      result = attribute.complete(result);
    }

    return result;
  }

  public FunctionCallTree functionCall(SyntaxToken name, SyntaxToken openParenthesis, Optional<SeparatedTrees<ExpressionTree>> arguments, SyntaxToken closeParenthesis) {
    return new FunctionCallTreeImpl(name, openParenthesis, arguments.orNull(), closeParenthesis);
  }

  public SeparatedTrees<ExpressionTree> functionCallArguments(
    ExpressionTree firstArgument,
    Optional<List<Pair<SyntaxToken, ExpressionTree>>> otherArguments,
    Optional<SyntaxToken> trailingToken) {
    return separatedTrees(firstArgument, otherArguments, trailingToken.orNull());
  }

  public PartialAttrSplatAccess partialAttrSplatAccess(SyntaxToken token, SyntaxToken token1) {
    return new PartialAttrSplatAccess(token, token1);
  }

  public PartialIndexSplatAccess partialIndexSplatAccess(SyntaxToken openBracket, SyntaxToken star, SyntaxToken closeBracket) {
    return new PartialIndexSplatAccess(openBracket, star, closeBracket);
  }

  public ForTupleTree forTuple(SyntaxToken openBracket,
    AbstractForTree.ForIntro intro,
    ExpressionTree expression,
    Optional<Pair<SyntaxToken, ExpressionTree>> condition,
    SyntaxToken closeBracket) {
    return new ForTupleTreeImpl(openBracket, intro, expression, condition.orNull(), closeBracket);
  }

  public ForObjectTree forObject(SyntaxToken openBrace,
    AbstractForTree.ForIntro intro,
    ExpressionTree firstExpression,
    SyntaxToken arrow,
    ExpressionTree secondExpression,
    Optional<SyntaxToken> ellipsis,
    Optional<Pair<SyntaxToken, ExpressionTree>> condition,
    SyntaxToken closeBrace) {
    return new ForObjectTreeImpl(openBrace, intro, firstExpression, arrow, secondExpression, ellipsis.orNull(), condition.orNull(), closeBrace);
  }

  public AbstractForTree.ForIntro forIntro(SyntaxToken forToken, SeparatedTrees<VariableExprTree> identifiers, SyntaxToken inToken,
    ExpressionTree inExpression, SyntaxToken colonToken) {
    return new AbstractForTree.ForIntro(forToken, identifiers, inToken, inExpression, colonToken);
  }

  public SeparatedTrees<VariableExprTree> forIntroIdentifiers(VariableExprTree first, Optional<Pair<SyntaxToken, VariableExprTree>> second) {
    List<VariableExprTree> elements = new ArrayList<>();
    List<SyntaxToken> separators = new ArrayList<>();

    elements.add(first);

    if (second.isPresent()) {
      separators.add(second.get().first());
      elements.add(second.get().second());
    }

    return new SeparatedTreesImpl<>(elements, separators);
  }

  public SyntaxToken quotedIdentifier(SyntaxToken openQuote, SyntaxToken identifier, SyntaxToken closingQuote) {
    return identifier;
  }

  private static <T extends TerraformTree> SeparatedTreesImpl<T> separatedTrees(
    T firstElement,
    Optional<List<Pair<SyntaxToken, T>>> pairs,
    @Nullable SyntaxToken trailingSeparator) {
    List<T> elements = new ArrayList<>();
    List<SyntaxToken> separators = new ArrayList<>();

    elements.add(firstElement);
    if (pairs.isPresent()) {
      for (Pair<SyntaxToken, T> tuple : pairs.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    if (trailingSeparator != null) {
      separators.add(trailingSeparator);
    }

    return new SeparatedTreesImpl<>(elements, separators);
  }

  public PartialAccess condition(SyntaxToken queryToken, ExpressionTree trueExpression, SyntaxToken colonToken, ExpressionTree falseExpression) {
    return new PartialCondition(queryToken, trueExpression, colonToken, falseExpression);
  }

  public ParenthesizedExpressionTree parenthesizedExpression(SyntaxToken openParenthesis, ExpressionTree expression, SyntaxToken closeParenthesis) {
    return new ParenthesizedExpressionTreeImpl(openParenthesis, expression, closeParenthesis);
  }

  public ExpressionTree binaryExpression(ExpressionTree firstExpression, Optional<List<Pair<SyntaxToken, ExpressionTree>>> zeroOrMore) {
    if (!zeroOrMore.isPresent()) {
      return firstExpression;
    }

    ExpressionTree result = firstExpression;
    for (Pair<SyntaxToken, ExpressionTree> t : zeroOrMore.get()) {
      result = new BinaryExpressionTreeImpl(result, t.first(), t.second());
    }

    return result;
  }

  public ExpressionTree prefixExpression(Optional<List<SyntaxToken>> prefixes, ExpressionTree expression) {
    if (!prefixes.isPresent()) {
      return expression;
    }

    ExpressionTree result = expression;
    List<SyntaxToken> reversedPrefixes = new ArrayList<>(prefixes.get());
    Collections.reverse(reversedPrefixes);
    for (SyntaxToken prefix : reversedPrefixes) {
      result = new PrefixExpressionTreeImpl(prefix, result);
    }

    return result;
  }

  public TemplateInterpolationTree templateInterpolation(SyntaxToken token, ExpressionTree expression, SyntaxToken token1) {
    return new TemplateInterpolationTreeImpl(token, expression, token1);
  }

  public ExpressionTree templateExpr(TerraformTree spacing, SyntaxToken openQuotes, List<ExpressionTree> oneOrMore, SyntaxToken closeQuotes) {
    return new TemplateExpressionTreeImpl(openQuotes, oneOrMore, closeQuotes);
  }

  public TemplateIfDirectiveTree templateIfDirective(
    TemplateIfDirectiveTreeImpl.IfPart ifPart,
    Optional<TemplateIfDirectiveTreeImpl.ElsePart> elsePart,
    SyntaxToken endIfOpenToken,
    SyntaxToken endIfToken,
    SyntaxToken endIfCloseToken) {
    return new TemplateIfDirectiveTreeImpl(ifPart, elsePart.orNull(), endIfOpenToken, endIfToken, endIfCloseToken);
  }

  public TemplateIfDirectiveTreeImpl.ElsePart templateIfDirectiveElsePart(
    SyntaxToken elseOpenToken,
    SyntaxToken elseToken,
    SyntaxToken elseCloseToken,
    ExpressionTree elseExpression) {
    return new TemplateIfDirectiveTreeImpl.ElsePart(elseOpenToken, elseToken, elseCloseToken, elseExpression);
  }

  public TemplateIfDirectiveTreeImpl.IfPart templateIfDirectiveIfPart(
    SyntaxToken ifOpenToken,
    SyntaxToken ifToken,
    ExpressionTree condition,
    SyntaxToken ifCloseToken,
    ExpressionTree trueExpression) {
    return new TemplateIfDirectiveTreeImpl.IfPart(ifOpenToken, ifToken, condition, ifCloseToken, trueExpression);
  }

  public TemplateForDirectiveTree templateForDirective(
    TemplateForDirectiveTreeImpl.Intro intro,
    ExpressionTree expression,
    SyntaxToken endForOpenToken,
    SyntaxToken endForToken,
    SyntaxToken endForCloseToken) {
    return new TemplateForDirectiveTreeImpl(intro, expression, endForOpenToken, endForToken, endForCloseToken);
  }

  public TemplateForDirectiveTreeImpl.Intro templateForDirectiveIntro(
    SyntaxToken forOpenToken,
    SyntaxToken forToken,
    SeparatedTrees<VariableExprTree> loopVariables,
    SyntaxToken inToken,
    ExpressionTree loopExpression,
    SyntaxToken forCloseToken) {
    return new TemplateForDirectiveTreeImpl.Intro(forOpenToken, forToken, loopVariables, inToken, loopExpression, forCloseToken);
  }

  public static class Pair<T, U> {

    private final T first;
    private final U second;

    public Pair(T first, U second) {
      super();

      this.first = first;
      this.second = second;
    }

    public T first() {
      return first;
    }

    public U second() {
      return second;
    }
  }

  public <T, U> Pair<T, U> newPair(T first, U second) {
    return new Pair<>(first, second);
  }

  public interface PartialAccess {
    ExpressionTree complete(ExpressionTree primary);
  }

  public static class PartialAttributeAccess implements PartialAccess {
    private final SyntaxToken accessToken;
    private final SyntaxToken attribute;

    public PartialAttributeAccess(SyntaxToken accessToken, SyntaxToken attribute) {
      this.accessToken = accessToken;
      this.attribute = attribute;
    }

    public AttributeAccessTree complete(ExpressionTree object) {
      return new AttributeAccessTreeImpl(object, accessToken, attribute);
    }
  }

  public static class PartialIndexAccess implements PartialAccess {
    private final SyntaxToken openBracket;
    private final ExpressionTree index;
    private final SyntaxToken closeBracket;

    public PartialIndexAccess(SyntaxToken openBracket, ExpressionTree index, SyntaxToken closeBracket) {
      this.openBracket = openBracket;
      this.index = index;
      this.closeBracket = closeBracket;
    }

    public IndexAccessExprTree complete(ExpressionTree subject) {
      return new IndexAccessExprTreeImpl(subject, openBracket, index, closeBracket);
    }
  }

  public static class PartialAttrSplatAccess implements PartialAccess {

    private final SyntaxToken dot;
    private final SyntaxToken star;

    public PartialAttrSplatAccess(SyntaxToken dot, SyntaxToken star) {
      this.dot = dot;
      this.star = star;
    }

    @Override
    public AttributeSplatAccessTree complete(ExpressionTree object) {
      return new AttributeSplatAccessTreeImpl(object, dot, star);
    }
  }

  public static class PartialIndexSplatAccess implements PartialAccess {

    private final SyntaxToken openBracket;
    private final SyntaxToken star;
    private final SyntaxToken closeBracket;

    public PartialIndexSplatAccess(SyntaxToken openBracket, SyntaxToken star, SyntaxToken closeBracket) {
      this.openBracket = openBracket;
      this.star = star;
      this.closeBracket = closeBracket;
    }

    @Override
    public IndexSplatAccessTree complete(ExpressionTree object) {
      return new IndexSplatAccessTreeImpl(object, openBracket, star, closeBracket);
    }
  }

  public static class PartialCondition implements PartialAccess {

    private final SyntaxToken queryToken;
    private final ExpressionTree trueExpression;
    private final SyntaxToken colonToken;
    private final ExpressionTree falseExpression;

    public PartialCondition(SyntaxToken queryToken, ExpressionTree trueExpression, SyntaxToken colonToken, ExpressionTree falseExpression) {
      this.queryToken = queryToken;
      this.trueExpression = trueExpression;
      this.colonToken = colonToken;
      this.falseExpression = falseExpression;
    }

    @Override
    public ExpressionTree complete(ExpressionTree conditionExpression) {
      return new ConditionTreeImpl(conditionExpression, queryToken, trueExpression, colonToken, falseExpression);
    }
  }
}
