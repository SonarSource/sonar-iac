package org.sonar.plugins.iac.terraform.api.tree;

public interface IndexSplatAccessTree extends ExpressionTree {
  ExpressionTree subject();
}
