/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.OptionTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class OptionTreeImpl extends DockerTreeImpl implements OptionTree {
  private final SyntaxToken dashes;
  private final SyntaxToken name;
  private final SyntaxToken equals;
  private final SyntaxToken value;

  public OptionTreeImpl(SyntaxToken dashes, SyntaxToken name, @Nullable SyntaxToken equals, @Nullable SyntaxToken value) {
    this.dashes = dashes;
    this.name = name;
    this.equals = equals;
    this.value = value;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(dashes);
    children.add(name);
    if (equals != null && value != null) {
      children.add(equals);
      children.add(value);
    }
    return children;
  }

  @Override
  public DockerTree.Kind getKind() {
    return DockerTree.Kind.OPTION;
  }

  @Override
  public SyntaxToken dashes() {
    return dashes;
  }

  @Override
  public SyntaxToken name() {
    return name;
  }

  @Override
  public SyntaxToken equals() {
    return equals;
  }

  @Override
  public SyntaxToken value() {
    return value;
  }
}
