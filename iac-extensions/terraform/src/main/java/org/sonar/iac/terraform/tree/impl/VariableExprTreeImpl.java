/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.VariableExprTree;

public class VariableExprTreeImpl extends TerraformTreeImpl implements VariableExprTree {
  private final SyntaxToken token;

  public VariableExprTreeImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String name() {
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(token);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_EXPR;
  }
}
