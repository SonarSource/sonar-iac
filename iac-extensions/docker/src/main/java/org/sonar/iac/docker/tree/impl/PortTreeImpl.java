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
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class PortTreeImpl extends DockerTreeImpl implements PortTree {
  private final SyntaxToken portAndProtocolKey;
  private final SyntaxToken port;
  private final SyntaxToken separator;
  private final SyntaxToken protocol;

  public PortTreeImpl(@Nullable SyntaxToken port, @Nullable SyntaxToken separator, @Nullable SyntaxToken protocol, @Nullable SyntaxToken portAndProtocolKey) {
    this.port = port;
    this.separator = separator;
    this.protocol = protocol;
    this.portAndProtocolKey = portAndProtocolKey;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    if (this.port != null) {
      children.add(this.port);
    }
    if (this.separator != null) {
      children.add(this.separator);
    }
    if (this.protocol != null) {
      children.add(this.protocol);
    }
    if (this.portAndProtocolKey != null) {
      children.add(this.portAndProtocolKey);
    }
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.PORT;
  }

  @Override
  public SyntaxToken portAndProtocolKey() {
    return portAndProtocolKey;
  }

  @Override
  public SyntaxToken port() {
    return port;
  }

  @Override
  public SyntaxToken separator() {
    return separator;
  }

  @Override
  public SyntaxToken protocol() {
    return protocol;
  }
}
