/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;

import java.util.Collections;
import java.util.List;

public class ScalarTreeImpl extends CloudformationTreeImpl implements ScalarTree {

  private final String value;
  private final String tag;
  private final Style style;

  public ScalarTreeImpl(String value, Style style, String tag, TextRange textRange, List<Comment> comments) {
    super(textRange, comments);
    this.value = value;
    this.style = style;
    this.tag = tag;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public String tag() {
    return tag;
  }
}
