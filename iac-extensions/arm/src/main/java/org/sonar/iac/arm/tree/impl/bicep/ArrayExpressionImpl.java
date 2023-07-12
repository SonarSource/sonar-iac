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
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.Tuple;

public class ArrayExpressionImpl extends AbstractArmTreeImpl implements ArrayExpression {
  private final SyntaxToken lBracket;
  @Nullable
  private final SyntaxToken firstNewLine;
  private final List<Tuple<Expression, SyntaxToken>> elements;
  private final SyntaxToken rBracket;

  public ArrayExpressionImpl(SyntaxToken lBracket, @Nullable SyntaxToken firstNewLine, List<Tuple<Expression, SyntaxToken>> elements, SyntaxToken rBracket) {
    this.lBracket = lBracket;
    this.firstNewLine = firstNewLine;
    this.elements = elements;
    this.rBracket = rBracket;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(lBracket);
    if (firstNewLine != null) {
      children.add(firstNewLine);
    }
    for (Tuple<Expression, SyntaxToken> t : elements) {
      children.add(t.first());
      children.add(t.second());
    }
    children.add(rBracket);
    return children;
  }

  @Override
  public List<Expression> elements() {
    return elements.stream().map(Tuple::first).collect(Collectors.toList());
  }
}
