/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.tree.impl;

import java.util.Collections;
import java.util.List;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.SyntaxToken;

public class SyntaxTokenImpl extends TerraformTreeImpl implements SyntaxToken {

  private final String value;
  private final List<Comment> comments;

  public SyntaxTokenImpl(String value, TextRange textRange, List<Comment> comments) {
    this.value = value;
    this.textRange = textRange;
    this.comments = comments;
  }


  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Comment> comments() {
    return comments;
  }

  @Override
  public Kind getKind() {
    return Kind.TOKEN;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }
}
