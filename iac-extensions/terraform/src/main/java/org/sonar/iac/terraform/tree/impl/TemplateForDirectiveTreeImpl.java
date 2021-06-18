/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TemplateForDirectiveTree;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class TemplateForDirectiveTreeImpl extends TerraformTreeImpl implements TemplateForDirectiveTree {
  private final Intro intro;
  private final ExpressionTree expression;
  private final SyntaxToken endForOpenToken;
  private final SyntaxToken endForToken;
  private final SyntaxToken endForCloseToken;

  public TemplateForDirectiveTreeImpl(Intro intro, ExpressionTree expression, SyntaxToken endForOpenToken, SyntaxToken endForToken, SyntaxToken endForCloseToken) {
    this.intro = intro;
    this.expression = expression;
    this.endForOpenToken = endForOpenToken;
    this.endForToken = endForToken;
    this.endForCloseToken = endForCloseToken;
  }

  @Override
  public SeparatedTrees<VariableExprTree> loopVariables() {
    return intro.loopVariables;
  }

  @Override
  public ExpressionTree loopExpression() {
    return intro.loopExpression;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return Kind.TEMPLATE_DIRECTIVE_FOR;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(intro.children());
    children.add(expression);
    children.add(endForOpenToken);
    children.add(endForToken);
    children.add(endForCloseToken);

    return children;
  }

  public static class Intro extends TerraformTreeImpl {
    private final SyntaxToken forOpenToken;
    private final SyntaxToken forToken;
    private final SeparatedTrees<VariableExprTree> loopVariables;
    private final SyntaxToken inToken;
    private final ExpressionTree loopExpression;
    private final SyntaxToken forCloseToken;

    public Intro(
      SyntaxToken forOpenToken,
      SyntaxToken forToken,
      SeparatedTrees<VariableExprTree> loopVariables,
      SyntaxToken inToken,
      ExpressionTree loopExpression,
      SyntaxToken forCloseToken) {
      this.forOpenToken = forOpenToken;
      this.forToken = forToken;
      this.loopVariables = loopVariables;
      this.inToken = inToken;
      this.loopExpression = loopExpression;
      this.forCloseToken = forCloseToken;
    }


    @Override
    public Kind getKind() {
      // this will never be used as this is just wrapper class to ease parsing
      return null;
    }

    @Override
    public List<Tree> children() {
      List<Tree> children = new ArrayList<>();
      children.add(forOpenToken);
      children.add(forToken);
      children.addAll(loopVariables.treesAndSeparators());
      children.add(inToken);
      children.add(loopExpression);
      children.add(forCloseToken);

      return children;
    }
  }
}
