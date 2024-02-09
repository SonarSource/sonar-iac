/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
