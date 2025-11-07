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

/**
 * NumberNode holds a number: signed or unsigned integer, float, or complex.
 * The value is parsed and stored under all the types that can represent the value.
 * This simulates in a small amount of code the behavior of Go's ideal constants.
 */
public interface NumberNode extends Node {
  @Override
  default NodeType type() {
    return NodeType.NODE_NUMBER;
  }

  /**
   * The original textual representation from the input.
   *
   * @return the original textual representation from the input
   */
  String text();
}
