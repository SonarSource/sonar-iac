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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Expression;

public class ArgumentImpl extends AbstractDockerTreeImpl implements Argument {

  private final List<Expression> expressions;

  public ArgumentImpl(List<Expression> expressions) {
    this.expressions = expressions;
  }

  @Override
  public List<Tree> children() {
    return new ArrayList<>(expressions);
  }

  @Override
  public List<Expression> expressions() {
    return expressions;
  }

  @Override
  public Kind getKind() {
    return DockerTree.Kind.ARGUMENT;
  }

  @Override
  public String toString() {
    return expressions.stream().map(Expression::toString).collect(Collectors.joining());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArgumentImpl argument)) {
      return false;
    }
    return Objects.equals(expressions, argument.expressions);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(expressions);
  }
}
