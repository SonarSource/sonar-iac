/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.tree.Comment;

import java.util.List;

public abstract class CloudformationTreeImpl implements CloudformationTree {
  protected final TextRange textRange;
  protected final List<Comment> comments;

  protected CloudformationTreeImpl(TextRange textRange, List<Comment> comments) {
    this.textRange = textRange;
    this.comments = comments;
  }

  @Override
  public TextRange textRange() {
    return textRange;
  }

  public List<Comment> comments() {
    return comments;
  }
}
