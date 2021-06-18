/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.AttributeSplatAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class AttributeSplatAccessTreeImpl extends TerraformTreeImpl implements AttributeSplatAccessTree {
  private final ExpressionTree object;
  private final SyntaxToken dot;
  private final SyntaxToken star;

  public AttributeSplatAccessTreeImpl(ExpressionTree object, SyntaxToken dot, SyntaxToken star) {
    this.object = object;
    this.dot = dot;
    this.star = star;
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE_SPLAT_ACCESS;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(object, dot, star);
  }

  @Override
  public ExpressionTree object() {
    return object;
  }

}
