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
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class AttributeTreeImpl extends TerraformTreeImpl implements AttributeTree {
  private final SyntaxToken key;
  private final SyntaxToken equalSign;
  private final ExpressionTree value;

  public AttributeTreeImpl(SyntaxToken key, SyntaxToken equalSign, ExpressionTree value) {
    this.key = key;
    this.equalSign = equalSign;
    this.value = value;
  }

  @Override
  public SyntaxToken key() {
    return key;
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
    return Arrays.asList(key, equalSign, value);
  }

  @Override
  public Kind getKind() {
    return Kind.ATTRIBUTE;
  }
}
