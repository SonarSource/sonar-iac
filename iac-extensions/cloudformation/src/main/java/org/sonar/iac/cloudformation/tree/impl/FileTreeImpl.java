/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.tree.impl;

import org.sonar.api.batch.fs.TextRange;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.cloudformation.api.tree.FileTree;
import org.sonar.iac.common.api.tree.Tree;

import java.util.Collections;
import java.util.List;

public class FileTreeImpl extends CloudformationTreeImpl implements FileTree {
  private final CloudformationTree root;

  public FileTreeImpl(CloudformationTree root, TextRange textRange) {
    // A file on its own has no comments. They will be attached to the root node.
    super(textRange, Collections.emptyList());
    this.root = root;
  }

  @Override
  public CloudformationTree root() {
    return root;
  }

  @Override
  public List<Tree> children() {
    if (root == null) {
      return Collections.emptyList();
    }
    return Collections.singletonList(root);
  }

  @Override
  public String tag() {
    return "FILE";
  }

}
