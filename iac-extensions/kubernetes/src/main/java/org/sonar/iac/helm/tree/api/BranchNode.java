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
package org.sonar.iac.helm.tree.api;

import org.jspecify.annotations.Nullable;

/**
 * BranchNode is the common representation of if, range, and with.
 */
public interface BranchNode extends Node {
  /**
   * The pipeline to be evaluated.
   *
   * @return the pipeline to be evaluated
   */
  @Nullable
  PipeNode pipe();

  /**
   * What to execute if the value is non-empty.
   *
   * @return the list of nodes to execute if the value is non-empty
   */
  @Nullable
  ListNode list();

  /**
   * What to execute if the value is empty (nil if absent).
   *
   * @return the list of nodes to execute if the value is empty
   */
  @Nullable
  ListNode elseList();
}
