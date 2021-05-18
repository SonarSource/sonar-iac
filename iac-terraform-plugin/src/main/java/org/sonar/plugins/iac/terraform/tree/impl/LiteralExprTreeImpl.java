package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

public class LiteralExprTreeImpl extends TerraformTree implements LiteralExprTree {
  private final SyntaxToken token;

  public LiteralExprTreeImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public SyntaxToken token() {
    return token;
  }

  @Override
  public String value() {
    return token.text();
  }
}
