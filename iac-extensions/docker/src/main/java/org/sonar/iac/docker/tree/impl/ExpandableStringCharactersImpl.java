/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import java.util.Objects;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ExpandableStringCharacters;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExpandableStringCharactersImpl extends AbstractDockerTreeImpl implements ExpandableStringCharacters {

  private final SyntaxToken token;

  public ExpandableStringCharactersImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public String value() {
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return List.of(token);
  }

  @Override
  public Kind getKind() {
    return Kind.EXPANDABLE_STRING_CHARACTERS;
  }

  @Override
  public String toString() {
    return token.value();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExpandableStringCharactersImpl that)) {
      return false;
    }
    return Objects.equals(token, that.token);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(token);
  }
}
