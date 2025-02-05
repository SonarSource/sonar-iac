/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ForTupleTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.parser.TreeFactory;

public class ForTupleTreeImpl extends AbstractForTree implements ForTupleTree {
  private final SyntaxToken openBracket;
  private final ExpressionTree expression;
  private final TreeFactory.Pair<SyntaxToken, ExpressionTree> condition;
  private final SyntaxToken closeBracket;

  public ForTupleTreeImpl(SyntaxToken openBracket, ForIntro intro, ExpressionTree expression, @Nullable TreeFactory.Pair<SyntaxToken, ExpressionTree> condition,
    SyntaxToken closeBracket) {
    super(intro);
    this.openBracket = openBracket;
    this.expression = expression;
    this.condition = condition;
    this.closeBracket = closeBracket;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Optional<ExpressionTree> condition() {
    return condition != null ? Optional.of(condition.second()) : Optional.empty();
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_TUPLE;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openBracket);
    children.addAll(intro.children());
    children.add(expression);
    if (condition != null) {
      children.add(condition.first());
      children.add(condition.second());
    }
    children.add(closeBracket);

    return children;
  }
}
