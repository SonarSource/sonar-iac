/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
