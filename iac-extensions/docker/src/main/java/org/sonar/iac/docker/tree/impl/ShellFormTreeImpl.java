package org.sonar.iac.docker.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ShellFormTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ShellFormTreeImpl extends DockerTreeImpl implements ShellFormTree {

  private final List<SyntaxToken> literals;

  public ShellFormTreeImpl(List<SyntaxToken> literals) {
    this.literals = literals;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(literals);
  }

  @Override
  public Kind getKind() {
    return Kind.SHELL_FORM;
  }

  @Override
  public List<SyntaxToken> literals() {
    return literals;
  }
}
