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
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Flag;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class FlagImpl extends AbstractDockerTreeImpl implements Flag {

  private final SyntaxToken prefix;
  private final SyntaxToken name;
  private final SyntaxToken equals;
  private final Argument value;

  public FlagImpl(SyntaxToken prefix, SyntaxToken name, @Nullable SyntaxToken equals, @Nullable Argument value) {
    this.prefix = prefix;
    this.name = name;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public String name() {
    return name.value();
  }

  @Nullable
  @Override
  public Argument value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(prefix);
    children.add(name);
    if (equals != null) {
      children.add(equals);
    }
    if (value != null) {
      children.add(value);
    }
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.PARAM;
  }

  @Override
  public String toString() {
    return "FlagImpl{" +
      "prefix=" + prefix +
      ", name=" + name +
      ", equals=" + equals +
      ", value=" + value +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FlagImpl flag)) {
      return false;
    }
    // By the grammar, flags always have same prefix and equals token
    return Objects.equals(name, flag.name) && Objects.equals(value, flag.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prefix, name, equals, value);
  }
}
