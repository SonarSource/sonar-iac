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
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class ObjectElementTreeImpl extends TerraformTreeImpl implements ObjectElementTree {
  private final ExpressionTree name;
  private final SyntaxToken equalOrColonSign;
  private final ExpressionTree value;

  public ObjectElementTreeImpl(ExpressionTree name, SyntaxToken equalOrColonSign, ExpressionTree value) {
    this.name = name;
    this.equalOrColonSign = equalOrColonSign;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(name, equalOrColonSign, value);
  }

  @Override
  public ExpressionTree name() {
    return name;
  }

  @Override
  public SyntaxToken equalOrColonSign() {
    return equalOrColonSign;
  }

  @Override
  public ExpressionTree value() {
    return value;
  }

  @Override
  public Kind getKind() {
    return Kind.OBJECT_ELEMENT;
  }
}
