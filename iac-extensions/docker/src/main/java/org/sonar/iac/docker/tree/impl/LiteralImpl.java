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
package org.sonar.iac.docker.tree.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class LiteralImpl extends AbstractDockerTreeImpl implements Literal {

  private final SyntaxToken token;

  public LiteralImpl(SyntaxToken token) {
    this.token = token;
  }

  @Override
  public String value() {
    String value = token.value();
    var isSingleQuoted = value.startsWith("'") && value.endsWith("'");
    var isDoubleQuoted = value.startsWith("\"") && value.endsWith("\"");
    if (isSingleQuoted || isDoubleQuoted) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  @Override
  public String originalValue() {
    return token.value();
  }

  @Override
  public List<Tree> children() {
    return Collections.singletonList(token);
  }

  @Override
  public Kind getKind() {
    return DockerTree.Kind.STRING_LITERAL;
  }

  @Override
  public String toString() {
    return token.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LiteralImpl literal)) {
      return false;
    }
    return Objects.equals(token, literal.token);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(token);
  }
}
