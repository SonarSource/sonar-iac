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

import java.util.ArrayList;
import java.util.List;
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
}
