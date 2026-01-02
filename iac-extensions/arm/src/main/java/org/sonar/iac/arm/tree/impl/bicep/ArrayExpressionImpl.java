/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.arm.tree.impl.bicep;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.SeparatedList;
import org.sonar.iac.common.api.tree.Tree;

public class ArrayExpressionImpl extends AbstractArmTreeImpl implements ArrayExpression {
  private final SyntaxToken lBracket;
  private final SeparatedList<Expression, SyntaxToken> elements;
  private final SyntaxToken rBracket;

  public ArrayExpressionImpl(SyntaxToken lBracket, SeparatedList<Expression, SyntaxToken> elements, SyntaxToken rBracket) {
    this.lBracket = lBracket;
    this.elements = elements;
    this.rBracket = rBracket;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(lBracket);
    children.addAll(elements.elementsAndSeparators());
    children.add(rBracket);
    return children;
  }

  @Override
  public List<Expression> elements() {
    return elements.elements();
  }
}
