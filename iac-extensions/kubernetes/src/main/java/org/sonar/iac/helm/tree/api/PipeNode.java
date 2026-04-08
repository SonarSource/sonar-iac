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

import java.util.List;

/**
 * PipeNode holds a pipeline with optional declaration
 */
public interface PipeNode extends Node {
  @Override
  default NodeType type() {
    return NodeType.NODE_PIPE;
  }

  /**
   * Variables in lexical order.
   *
   * @return the variables in lexical order
   */
  List<VariableNode> declarations();

  /**
   * The commands in lexical order.
   *
   * @return the commands in lexical order
   */
  List<CommandNode> commands();
}
