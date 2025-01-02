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
