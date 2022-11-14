package org.sonar.iac.docker.tree.api;

import org.sonar.iac.common.api.tree.Tree;

public interface StringLiteral extends Tree {
  String literal();
}
