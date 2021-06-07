/*
 * SonarQube IaC Plugin
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
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.Tree;
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
