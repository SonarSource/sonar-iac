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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.ExposeTree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class ExposeTreeImpl extends DockerTreeImpl implements ExposeTree {
  private final SyntaxToken exposeToken;
  private final List<PortTree> ports;

  public ExposeTreeImpl(SyntaxToken exposeToken, List<PortTree> ports) {
    this.exposeToken = exposeToken;
    this.ports = ports;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(exposeToken);
    children.addAll(ports);
    return children;
  }
  @Override
  public Kind getKind() {
    return Kind.EXPOSE;
  }

  @Override
  public SyntaxToken exposeToken() {
    return exposeToken;
  }

  @Override
  public List<PortTree> ports() {
    return ports;
  }
}
