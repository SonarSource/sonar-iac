/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
