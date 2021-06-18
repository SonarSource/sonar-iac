/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class SequenceTreeImpl extends CloudformationTreeImpl implements SequenceTree {
  private final List<CloudformationTree> elements;
  private final String tag;

  public SequenceTreeImpl(List<CloudformationTree> elements, String tag, TextRange textRange, List<Comment> comments) {
    super(textRange, comments);
    this.elements = elements;
    this.tag = tag;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(elements);
  }

  @Override
  public List<CloudformationTree> elements() {
    return elements;
  }

  @Override
  public String tag() {
    return tag;
  }
}
