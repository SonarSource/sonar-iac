/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.BodyTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class FileTreeImpl extends TerraformTreeImpl implements FileTree {
  private final Optional<BodyTree> body;
  private final SyntaxToken eof;

  public FileTreeImpl(BodyTree body, SyntaxToken eof) {
    this.body = Optional.ofNullable(body);
    this.eof = eof;
  }

  @Override
  public Optional<BodyTree> body() {
    return body;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    body.ifPresent(children::add);
    children.add(eof);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }
}
