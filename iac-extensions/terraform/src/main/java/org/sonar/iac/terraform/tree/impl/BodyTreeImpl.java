/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BodyTree;

public class BodyTreeImpl extends TerraformTreeImpl implements BodyTree {
  private final List<Tree> statements;

  public BodyTreeImpl(List<Tree> statements) {
    this.statements = statements;
  }

  @Override
  public List<Tree> statements() {
    return statements;
  }

  @Override
  public List<Tree> children() {
    return statements;
  }

  @Override
  public Kind getKind() {
    return Kind.BODY;
  }
}
