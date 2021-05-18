package org.sonar.plugins.iac.terraform.api.tree;

import org.sonar.plugins.iac.terraform.api.tree.lexical.SyntaxToken;

public interface LiteralExprTree extends ExpressionTree {
  SyntaxToken token();
  String value();
}
