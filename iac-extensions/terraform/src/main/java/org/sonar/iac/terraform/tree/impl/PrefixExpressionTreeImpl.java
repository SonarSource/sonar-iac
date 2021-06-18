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
import org.sonar.iac.terraform.api.tree.PrefixExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class PrefixExpressionTreeImpl extends TerraformTreeImpl implements PrefixExpressionTree {
  private final SyntaxToken prefix;
  private final ExpressionTree expression;

  public PrefixExpressionTreeImpl(SyntaxToken prefix, ExpressionTree expression) {
    this.prefix = prefix;
    this.expression = expression;
  }

  @Override
  public SyntaxToken prefix() {
    return prefix;
  }

  @Override
  public ExpressionTree expression() {
    return expression;
  }

  @Override
  public Kind getKind() {
    return Kind.PREFIX_EXPRESSION;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(prefix, expression);
  }
}
