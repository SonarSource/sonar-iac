/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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
 * NodeType represents the type of a node in the Go template AST.
 */
public enum NodeType {
  NODE_UNKNOWN,
  NODE_TEXT,
  NODE_ACTION,
  NODE_BOOL,
  NODE_CHAIN,
  NODE_COMMAND,
  NODE_DOT,
  NODE_ELSE,
  NODE_END,
  NODE_FIELD,
  NODE_IDENTIFIER,
  NODE_IF,
  NODE_LIST,
  NODE_NIL,
  NODE_NUMBER,
  NODE_PIPE,
  NODE_RANGE,
  NODE_STRING,
  NODE_TEMPLATE,
  NODE_VARIABLE,
  NODE_WITH,
  NODE_COMMENT,
  NODE_BREAK,
  NODE_CONTINUE;
}
