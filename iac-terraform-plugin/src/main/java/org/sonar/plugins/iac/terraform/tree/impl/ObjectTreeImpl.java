package org.sonar.plugins.iac.terraform.tree.impl;

import org.sonar.plugins.iac.terraform.api.tree.ObjectTree;
import org.sonar.plugins.iac.terraform.api.tree.Tree;
import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

import java.util.Arrays;
import java.util.List;

public class ObjectTreeImpl extends TerraformTree implements ObjectTree {
  private final SyntaxToken openBrace;
  private final SyntaxToken closeBrace;

  public ObjectTreeImpl(SyntaxToken openBrace, SyntaxToken closeBrace) {
    this.openBrace = openBrace;
    this.closeBrace = closeBrace;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(openBrace, closeBrace);
  }
}
