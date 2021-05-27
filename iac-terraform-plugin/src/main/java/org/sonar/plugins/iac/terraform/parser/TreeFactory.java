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

import com.sonar.sslr.api.typed.Optional;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.plugins.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.AttributeSplatAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.AttributeTree;
import org.sonar.plugins.iac.terraform.api.tree.BlockTree;
import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.FileTree;
import org.sonar.plugins.iac.terraform.api.tree.FunctionCallTree;
import org.sonar.plugins.iac.terraform.api.tree.IndexAccessExprTree;
import org.sonar.plugins.iac.terraform.api.tree.IndexSplatAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.LabelTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.TupleTree;
import org.sonar.plugins.iac.terraform.api.tree.VariableExprTree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeAccessTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeSplatAccessTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BodyTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.FileTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.FunctionCallTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.IndexAccessExprTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.IndexSplatAccessTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.ObjectElementTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.ObjectTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.OneLineBlockTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.SeparatedTreesImpl;
import org.sonar.plugins.iac.terraform.tree.impl.TupleTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.VariableExprTreeImpl;

public class TreeFactory {
  public FileTree file(Optional<BodyTree> body, Optional<SyntaxToken> spacing, SyntaxToken eof) {
    return new FileTreeImpl(body.orNull(), eof);
  }

  public BodyTree body(List<Tree> statements) {
    return new BodyTreeImpl(statements);
  }

  public BlockTree block(SyntaxToken type, Optional<List<LabelTree>> labels, SyntaxToken openBrace, SyntaxToken newLine, Optional<BodyTree> body, SyntaxToken closeBrace) {
    return new BlockTreeImpl(type, labels.orNull(), openBrace, body.orNull(), closeBrace);
  }

  public OneLineBlockTree oneLineBlock(SyntaxToken type, Optional<List<LabelTree>> labels, SyntaxToken openBrace, Optional<AttributeTree> attribute, SyntaxToken closeBrace) {
    return new OneLineBlockTreeImpl(type, labels.orNull(), openBrace, attribute.orNull(), closeBrace);
  }

  public LabelTree label(SyntaxToken token) {
    return new LabelTreeImpl(token);
  }

  public LiteralExprTreeImpl numericLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Tree.Kind.NUMERIC_LITERAL, token);
  }

  public LiteralExprTreeImpl booleanLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Tree.Kind.BOOLEAN_LITERAL, token);
  }

  public LiteralExprTreeImpl nullLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Tree.Kind.NULL_LITERAL, token);
  }

  public LiteralExprTreeImpl stringLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Tree.Kind.STRING_LITERAL, token);
  }

  public LiteralExprTreeImpl heredocLiteral(SyntaxToken token) {
    return new LiteralExprTreeImpl(Tree.Kind.HEREDOC_LITERAL, token);
  }

  public AttributeTree attribute(SyntaxToken name, SyntaxToken equalSign, ExpressionTree value) {
    return new AttributeTreeImpl(name, equalSign, value);
  }

  public ObjectTree object(SyntaxToken openBrace, Optional<SeparatedTrees<ObjectElementTree>> elements, SyntaxToken closeBrace) {
    return new ObjectTreeImpl(openBrace, elements.orNull(), closeBrace);
  }

  public ObjectElementTree objectElement(ExpressionTree name, SyntaxToken equalOrColonSign, ExpressionTree value) {
    return new ObjectElementTreeImpl(name, equalOrColonSign, value);
  }

  public SeparatedTrees<ObjectElementTree> objectElements(
    ObjectElementTree firstElement,
    Optional<List<Pair<SyntaxToken, ObjectElementTree>>> otherElements,
    Optional<InternalSyntaxToken> trailingComma) {
    return separatedTrees(firstElement, otherElements, trailingComma.orNull());
  }

  public PartialAttributeAccess partialAttributeAccess(SyntaxToken accessToken, SyntaxToken attribute) {
    return new PartialAttributeAccess(accessToken, attribute);
  }

  public VariableExprTree variable(InternalSyntaxToken token) {
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
    for (PartialAccess attribute: accesses.subList(1, accesses.size())) {
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

  private static <T extends Tree> SeparatedTreesImpl<T> separatedTrees(
    T firstElement,
    Optional<List<Pair<SyntaxToken, T>>> pairs,
    @Nullable SyntaxToken trailingSeparator
  ) {
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
}
