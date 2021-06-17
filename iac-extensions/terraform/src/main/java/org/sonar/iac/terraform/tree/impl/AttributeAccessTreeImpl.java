/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Arrays;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class AttributeAccessTreeImpl extends TerraformTreeImpl implements AttributeAccessTree {
  private final ExpressionTree object;
  private final SyntaxToken accessToken;
  private final SyntaxToken attribute;

  public AttributeAccessTreeImpl(ExpressionTree object, SyntaxToken accessToken, SyntaxToken attribute) {
    this.object = object;
    this.accessToken = accessToken;
    this.attribute = attribute;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(object, accessToken, attribute);
  }

  @Override
  public ExpressionTree object() {
    return object;
  }

  @Override
  public SyntaxToken attribute() {
    return attribute;
  }

  @Override
  public SyntaxToken accessToken() {
    return accessToken;
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE_ACCESS;
  }
}
