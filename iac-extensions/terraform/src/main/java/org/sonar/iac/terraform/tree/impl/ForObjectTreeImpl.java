/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ForObjectTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.parser.TreeFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForObjectTreeImpl extends AbstractForTree implements ForObjectTree {
  private final SyntaxToken openBrace;
  private final ExpressionTree firstExpression;
  private final SyntaxToken arrow;
  private final ExpressionTree secondExpression;
  private final SyntaxToken ellipsis;
  private final TreeFactory.Pair<SyntaxToken, ExpressionTree> condition;
  private final SyntaxToken closeBrace;

  public ForObjectTreeImpl(SyntaxToken openBrace, ForIntro intro, ExpressionTree firstExpression, SyntaxToken arrow, ExpressionTree secondExpression,
    @Nullable SyntaxToken ellipsis, @Nullable TreeFactory.Pair<SyntaxToken, ExpressionTree> condition, SyntaxToken closeBrace) {
    super(intro);
    this.openBrace = openBrace;
    this.firstExpression = firstExpression;
    this.arrow = arrow;
    this.secondExpression = secondExpression;
    this.ellipsis = ellipsis;
    this.condition = condition;
    this.closeBrace = closeBrace;
  }

  @Override
  public ExpressionTree firstExpression() {
    return firstExpression;
  }

  @Override
  public ExpressionTree secondExpression() {
    return secondExpression;
  }

  @Override
  public boolean hasEllipsis() {
    return ellipsis != null;
  }

  @Override
  public Optional<ExpressionTree> condition() {
    return condition != null ? Optional.of(condition.second()) : Optional.empty();
  }

  @Override
  public Kind getKind() {
    return Kind.FOR_OBJECT;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(openBrace);
    children.addAll(intro.children());
    children.add(firstExpression);
    children.add(arrow);
    children.add(secondExpression);
    if (ellipsis != null) {
      children.add(ellipsis);
    }
    if (condition != null) {
      children.add(condition.first());
      children.add(condition.second());
    }
    children.add(closeBrace);

    return children;
  }

}
