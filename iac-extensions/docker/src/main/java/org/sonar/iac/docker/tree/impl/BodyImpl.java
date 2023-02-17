/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;

public class BodyImpl extends AbstractDockerTreeImpl implements Body {

  private final List<ArgInstruction> globalArgs;
  private final List<DockerImage> dockerImages;
  private Scope scope;

  public BodyImpl(List<ArgInstruction> globalArgs, List<DockerImage> dockerImages) {
    this.globalArgs = globalArgs;
    this.dockerImages = dockerImages;
  }

  @Override
  public List<ArgInstruction> globalArgs() {
    return globalArgs;
  }

  @Override
  public List<DockerImage> dockerImages() {
    return dockerImages;
  }

  @Override
  public List<Tree> children() {
    List<Tree> children = new ArrayList<>();
    children.addAll(globalArgs);
    children.addAll(dockerImages);
    return children;
  }

  @Override
  public Kind getKind() {
    return Kind.BODY;
  }

  @Override
  public void setScope(Scope scope) {
    if (this.scope != null) {
      throw new IllegalArgumentException("A scope is already set");
    }
    this.scope = scope;
  }

  @Override
  public Scope scope() {
    return scope;
  }
}
