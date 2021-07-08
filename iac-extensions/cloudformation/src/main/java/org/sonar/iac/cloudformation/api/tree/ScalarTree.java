/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

import org.sonar.iac.common.api.tree.TextTree;

public interface ScalarTree extends TextTree, CloudformationTree {
  enum Style {
    DOUBLE_QUOTED,
    SINGLE_QUOTED,
    LITERAL,
    FOLDED,
    PLAIN,
    OTHER
  }

  Style style();
}
