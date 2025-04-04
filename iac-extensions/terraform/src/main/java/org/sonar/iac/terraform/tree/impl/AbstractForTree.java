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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.ForTree;
import org.sonar.iac.terraform.api.tree.SeparatedTrees;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public abstract class AbstractForTree extends TerraformTreeImpl implements ForTree {
  protected final ForIntro intro;

  protected AbstractForTree(ForIntro intro) {
    this.intro = intro;
  }

  public SeparatedTrees<VariableExprTree> loopVariables() {
    return intro.loopVariables;
  }

  public ExpressionTree loopExpression() {
    return intro.inExpression;
  }

  /**
   * Helper class to wrap the intro common to both for loop styles
   */
  public static class ForIntro {
    private final SyntaxToken forToken;
    private final SeparatedTrees<VariableExprTree> loopVariables;
    private final SyntaxToken inToken;
    private final ExpressionTree inExpression;
    private final SyntaxToken colonToken;

    public ForIntro(SyntaxToken forToken, SeparatedTrees<VariableExprTree> loopVariables, SyntaxToken inToken, ExpressionTree inExpression, SyntaxToken colonToken) {
      this.forToken = forToken;
      this.loopVariables = loopVariables;
      this.inToken = inToken;
      this.inExpression = inExpression;
      this.colonToken = colonToken;
    }

    public List<Tree> children() {
      List<Tree> children = new ArrayList<>();
      children.add(forToken);
      children.addAll(loopVariables.treesAndSeparators());
      children.add(inToken);
      children.add(inExpression);
      children.add(colonToken);

      return children;
    }
  }
}
