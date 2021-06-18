/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

import java.util.ArrayList;
import java.util.List;

public class MappingTreeImpl extends CloudformationTreeImpl implements MappingTree {
  private final List<TupleTree> elements;
  private final String tag;

  public MappingTreeImpl(List<TupleTree> elements, String tag, TextRange textRange, List<Comment> comments) {
    super(textRange, comments);
    this.elements = elements;
    this.tag = tag;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(elements);
  }

  @Override
  public List<TupleTree> elements() {
    return elements;
  }

  @Override
  public String tag() {
    return tag;
  }
}
