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
package org.sonar.iac.docker.tree.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.iac.common.api.tree.Comment;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class SyntaxTokenImpl extends AbstractDockerTreeImpl implements SyntaxToken {

  private final String value;
  private final List<Comment> comments;

  public SyntaxTokenImpl(String value, TextRange textRange, List<Comment> comments) {
    this.value = value;
    this.textRange = textRange;
    this.comments = comments;
  }

  @Override
  public TextRange textRange() {
    return textRange;
  }

  @Override
  public List<Tree> children() {
    return Collections.emptyList();
  }

  @Override
  public Kind getKind() {
    return Kind.TOKEN;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public List<Comment> comments() {
    return comments;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SyntaxTokenImpl that)) {
      return false;
    }
    return Objects.equals(value, that.value) && Objects.equals(comments, that.comments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, comments);
  }
}
