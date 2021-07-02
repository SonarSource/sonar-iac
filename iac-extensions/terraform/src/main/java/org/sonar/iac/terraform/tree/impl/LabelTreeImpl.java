/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class LabelTreeImpl extends TerraformTreeImpl implements LabelTree {
  private final SyntaxToken token;

  public LabelTreeImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String value() {
    //TODO: Label value should be given without quotes.
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }

  @Override
  public Kind getKind() {
    return Kind.LABEL;
  }
}
