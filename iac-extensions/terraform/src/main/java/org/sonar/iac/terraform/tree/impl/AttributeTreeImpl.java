/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class AttributeTreeImpl extends TerraformTreeImpl implements AttributeTree {
  private final SyntaxToken name;
  private final SyntaxToken equalSign;
  private final ExpressionTree value;

  public AttributeTreeImpl(SyntaxToken name, SyntaxToken equalSign, ExpressionTree value) {
    this.name = name;
    this.equalSign = equalSign;
    this.value = value;
  }

  @Override
  public SyntaxToken name() {
    return name;
  }

  @Override
  public SyntaxToken equalSign() {
    return equalSign;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(name, equalSign, value);
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE;
  }
}
