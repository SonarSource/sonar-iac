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
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class PortTreeImpl extends DockerTreeImpl implements PortTree {
  private final SyntaxToken portMin;
  private final SyntaxToken separatorPort;
  private final SyntaxToken portMax;
  private final SyntaxToken separatorProtocol;
  private final SyntaxToken protocol;

  public PortTreeImpl(SyntaxToken portMin, @Nullable SyntaxToken separatorPort, SyntaxToken portMax, @Nullable SyntaxToken separatorProtocol, @Nullable SyntaxToken protocol) {
    this.portMin = portMin;
    this.separatorPort = separatorPort;
    this.portMax = portMax;
    this.separatorProtocol = separatorProtocol;
    this.protocol = protocol;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.add(this.portMin);
    // range format : 'portmin-portmax'
    if (this.portMin != this.portMax) {
      children.add(this.separatorPort);
      children.add(this.portMax);
    }
    if (this.separatorProtocol != null) {
      children.add(this.separatorProtocol);
    }
    if (this.protocol != null) {
      children.add(this.protocol);
    }
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.PORT;
  }

  @Override
  public SyntaxToken portMin() {
    return portMin;
  }

  @Override
  public SyntaxToken portMax() {
    return portMax;
  }

  @Override
  @CheckForNull
  public SyntaxToken protocol() {
    return protocol;
  }
}
