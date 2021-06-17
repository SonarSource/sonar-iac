/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.common.extension.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import org.sonar.iac.common.api.tree.Tree;

public class TreeContext {

  private final Deque<Tree> ancestors;
  private Tree current;

  public TreeContext() {
    ancestors = new ArrayDeque<>();
  }

  public Deque<Tree> ancestors() {
    return ancestors;
  }

  public void before() {
    ancestors.clear();
  }

  public void enter(Tree node) {
    if (current != null) {
      ancestors.push(current);
    }
    current = node;
  }

  public void leave() {
    if (!ancestors.isEmpty()) {
      current = ancestors.pop();
    }
  }
}
