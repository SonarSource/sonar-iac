/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.jspecify.annotations.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;

public class BodyImpl extends AbstractDockerTreeImpl implements Body {

  private final List<ArgInstruction> globalArgs;
  private final List<DockerImage> dockerImages;
  @Nullable
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

  /**
   * The scope is assigned by {@code DockerSymbolVisitor} before any check runs, so it is always set by the time
   * this is called, honouring the non-null {@link org.sonar.iac.docker.tree.api.HasScope#scope()} contract.
   */
  @Override
  public Scope scope() {
    return Objects.requireNonNull(scope, "Scope is not set yet");
  }
}
