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

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.PortTree;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class PortTreeImpl extends DockerTreeImpl implements PortTree {
  private final SyntaxToken portAndProtocol;
  private final Integer port;
  private final String protocol;

  public PortTreeImpl(SyntaxToken portAndProtocol) {
    this.portAndProtocol = portAndProtocol;
    String str = portAndProtocol.value().replace("\"", "");
    String[] splitted = str.split("/");
    if (splitted.length == 2) {
      this.port = tryParseInt(splitted[0]);
      this.protocol = splitted[1];
    } else {
      this.port = tryParseInt(splitted[0]);
      this.protocol = null;
    }
  }

  private static Integer tryParseInt(String str) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public List<Tree> children() {
    return List.of(portAndProtocol);
  }

  @Override
  public Kind getKind() {
    return Kind.PORT;
  }

  @Override
  public SyntaxToken portAndProtocol() {
    return portAndProtocol;
  }

  @Override
  public Integer port() {
    return port;
  }

  @Override
  public String protocol() {
    return protocol;
  }
}
