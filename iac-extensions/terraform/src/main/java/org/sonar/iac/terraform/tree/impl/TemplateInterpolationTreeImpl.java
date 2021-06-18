/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TemplateInterpolationTree;

public class TemplateInterpolationTreeImpl extends TerraformTreeImpl implements TemplateInterpolationTree {
  private final SyntaxToken openDollarCurlyBraceToken;
  private final ExpressionTree expression;
  private final SyntaxToken closeCurlyBraceToken;

  public TemplateInterpolationTreeImpl(SyntaxToken openDollarCurlyBraceToken, ExpressionTree expression, SyntaxToken closeCurlyBraceToken) {
    this.openDollarCurlyBraceToken = openDollarCurlyBraceToken;
    this.expression = expression;
    this.closeCurlyBraceToken = closeCurlyBraceToken;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return Kind.TEMPLATE_INTERPOLATION;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(openDollarCurlyBraceToken, expression, closeCurlyBraceToken);
  }

}
