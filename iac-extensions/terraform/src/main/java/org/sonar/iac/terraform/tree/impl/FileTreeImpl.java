/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;
import org.sonar.iac.terraform.api.tree.TerraformTree;

public class FileTreeImpl extends TerraformTreeImpl implements FileTree {
  private final List<TerraformTree> statements;
  private final SyntaxToken eof;

  public FileTreeImpl(List<TerraformTree> statements, SyntaxToken eof) {
    this.statements = statements;
    this.eof = eof;
  }

  @Override
  public List<TerraformTree> statements() {
    return statements;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>(statements);
    children.add(eof);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }
}
