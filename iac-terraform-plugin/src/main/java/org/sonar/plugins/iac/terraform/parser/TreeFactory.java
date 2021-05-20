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
import org.sonar.plugins.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.plugins.iac.terraform.api.tree.AttributeTree;
import org.sonar.plugins.iac.terraform.api.tree.BlockTree;
import org.sonar.plugins.iac.terraform.api.tree.BodyTree;
import org.sonar.plugins.iac.terraform.api.tree.ExpressionTree;
import org.sonar.plugins.iac.terraform.api.tree.FileTree;
import org.sonar.plugins.iac.terraform.api.tree.LabelTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.OneLineBlockTree;
import org.sonar.plugins.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.iac.terraform.parser.lexical.InternalSyntaxToken;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeAccessTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.AttributeTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BlockTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.BodyTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.FileTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LabelTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.LiteralExprTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.ObjectElementTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.ObjectTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.OneLineBlockTreeImpl;
import org.sonar.plugins.iac.terraform.tree.impl.SeparatedTreesImpl;
import org.sonar.plugins.iac.terraform.tree.impl.VariableExprTreeImpl;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

  public LiteralExprTreeImpl literalExpr(SyntaxToken token) {
    return new LiteralExprTreeImpl(token);
  }

  public AttributeTree attribute(SyntaxToken name, SyntaxToken equalSign, ExpressionTree value) {
    return new AttributeTreeImpl(name, equalSign, value);
  }

  public ObjectTree object(SyntaxToken openBrace, Optional<SeparatedTrees<ObjectElementTree>> elements, SyntaxToken closeBrace) {
    return new ObjectTreeImpl(openBrace, elements.orNull(), closeBrace);
  }

  public ObjectElementTree objectElement(Tree name, SyntaxToken equalOrColonSign, ExpressionTree value) {
    return new ObjectElementTreeImpl(name, equalOrColonSign, value);
  }

  public SeparatedTrees<ObjectElementTree> objectElements(
    ObjectElementTree firstElement,
    Optional<List<Tuple<SyntaxToken, ObjectElementTree>>> otherElements,
    Optional<InternalSyntaxToken> trailingComma) {
    return separatedTrees(firstElement, otherElements, trailingComma.orNull());
  }

  private static <T extends Tree> SeparatedTreesImpl<T> separatedTrees(
    T firstElement,
    Optional<List<Tuple<SyntaxToken, T>>> tuples,
    @Nullable SyntaxToken trailingSeparator
  ) {
    List<T> elements = new ArrayList<>();
    List<SyntaxToken> separators = new ArrayList<>();

    elements.add(firstElement);
    if (tuples.isPresent()) {
      for (Tuple<SyntaxToken, T> tuple : tuples.get()) {
        separators.add(tuple.first());
        elements.add(tuple.second());
      }
    }

    if (trailingSeparator != null) {
      separators.add(trailingSeparator);
    }

    return new SeparatedTreesImpl<>(elements, separators);
  }

  public AttributeAccessTree attributeAccess(SyntaxToken accessToken, SyntaxToken attribute) {
    return new AttributeAccessTreeImpl(accessToken, attribute);
  }

  public AttributeAccessTree memberExpression(ExpressionTree object, List<AttributeAccessTree> attributeAccesses) {
    AttributeAccessTreeImpl result = (AttributeAccessTreeImpl) attributeAccesses.get(0);
    result.setObject(object);

    for (AttributeAccessTree attribute: attributeAccesses.subList(1, attributeAccesses.size())) {
      ((AttributeAccessTreeImpl)attribute).setObject(result);
      result = (AttributeAccessTreeImpl)attribute;
    }

    return result;
  }

  public ExpressionTree variable(InternalSyntaxToken token) {
    return new VariableExprTreeImpl(token);
  }

  public static class Tuple<T, U> {

    private final T first;
    private final U second;

    public Tuple(T first, U second) {
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

  public <T, U> Tuple<T, U> newTuple(T first, U second) {
    return new Tuple<>(first, second);
  }
}
