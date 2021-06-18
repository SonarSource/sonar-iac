/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Collections;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class LiteralExprTreeImpl extends TerraformTreeImpl implements LiteralExprTree {

  private final Kind kind;
  private final SyntaxToken token;

  public LiteralExprTreeImpl(Kind kind, SyntaxToken token) {
    this.kind = kind;
    this.token = token;
  }

  @Override
  public Kind getKind() {
    return kind;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String value() {
    if (is(Kind.STRING_LITERAL)) {
      return token.value().substring(1, token.value().length() - 1 );
    }
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }
}
