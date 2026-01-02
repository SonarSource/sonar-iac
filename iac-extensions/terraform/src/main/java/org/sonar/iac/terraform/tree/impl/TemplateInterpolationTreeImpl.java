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
