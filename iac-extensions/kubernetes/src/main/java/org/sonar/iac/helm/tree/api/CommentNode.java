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

import org.sonar.iac.common.api.tree.Comment;

/**
 * CommentNode holds a comment.
 */
public interface CommentNode extends Node, Comment {
  int COMMENT_PREFIX_LENGTH = "/*".length();
  int COMMENT_SUFFIX_LENGTH = "*/".length();

  @Override
  default NodeType type() {
    return NodeType.NODE_COMMENT;
  }

  @Override
  default String contentText() {
    var text = value().trim();
    // According to https://pkg.go.dev/text/template#hdr-Actions, comments in Go templates can only start with `/*` and end with `*/`
    return text.substring(COMMENT_PREFIX_LENGTH, text.length() - COMMENT_SUFFIX_LENGTH).trim();
  }
}
