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
package org.sonar.iac.docker.symbols;

import org.sonar.iac.docker.tree.api.DockerTree;

public class Usage {

  public enum Kind {
    ASSIGNMENT,
    ACCESS
  }

  private final Scope scope;

  private final DockerTree tree;

  private final Kind kind;

  public Usage(Scope scope, DockerTree tree, Kind kind) {
    this.scope = scope;
    this.tree = tree;
    this.kind = kind;
  }

  public Scope scope() {
    return scope;
  }

  public DockerTree tree() {
    return tree;
  }

  public Kind kind() {
    return kind;
  }
}
