/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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

/**
 * FieldNode holds a field (identifier starting with '.').
 * The names may be chained ('.x.y').
 * The period is dropped from each ident.
 */
public interface FieldNode extends Node {
  @Override
  default NodeType type() {
    return NodeType.NODE_FIELD;
  }

  /**
   * The identifiers in lexical order.
   *
   * @return the identifiers in lexical order
   */
  List<String> identifiers();
}
