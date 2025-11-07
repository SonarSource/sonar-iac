/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import java.util.Optional;

/**
 * ChainNode holds a term followed by a chain of field accesses (identifier starting with '.').
 * The names may be chained ('.x.y'). The periods are dropped from each ident.
 * In newer versions of Go (at least 1.21), doesn't appear in the AST in common cases.
 */
public interface ChainNode extends Node {
  @Override
  default NodeType type() {
    return NodeType.NODE_CHAIN;
  }

  /**
   * The term of the chain, or null if there is no term.
   *
   * @return the term of the chain, or null if there is no term
   */
  Optional<Node> node();

  /**
   * The identifiers in lexical order.
   *
   * @return the identifiers in lexical order
   */
  List<String> fields();
}
