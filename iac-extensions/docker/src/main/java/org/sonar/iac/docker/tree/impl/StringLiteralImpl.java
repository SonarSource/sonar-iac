package org.sonar.iac.docker.tree.impl;

import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.StringLiteral;

public class StringLiteralImpl implements StringLiteral {
  private final String literal;

  public StringLiteralImpl(String literal) {
    this.literal = literal;
  }

  @Override
  public String literal() {
    return literal;
  }

  @Override
  public TextRange textRange() {
    return null;
  }

  @Override
  public List<Tree> children() {
    return null;
  }
}
