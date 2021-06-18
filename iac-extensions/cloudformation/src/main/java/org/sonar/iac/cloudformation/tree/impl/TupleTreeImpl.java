/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.tree.Tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TupleTreeImpl extends CloudformationTreeImpl implements TupleTree {
  private final CloudformationTree key;
  private final CloudformationTree value;

  public TupleTreeImpl(CloudformationTree key, CloudformationTree value, TextRange textRange) {
    // Comments are attached to the key and value trees separately
    super(textRange, Collections.emptyList());
    this.key = key;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    return Arrays.asList(key, value);
  }

  @Override
  public CloudformationTree key() {
    return key;
  }

  @Override
  public CloudformationTree value() {
    return value;
  }

  @Override
  public String tag() {
    return "TUPLE";
  }
}
