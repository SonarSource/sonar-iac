/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.api.tree;

public interface ScalarTree extends CloudformationTree {
  enum Style {
    DOUBLE_QUOTED,
    SINGLE_QUOTED,
    LITERAL,
    FOLDED,
    PLAIN,
    OTHER
  }

  String value();
  Style style();
}
