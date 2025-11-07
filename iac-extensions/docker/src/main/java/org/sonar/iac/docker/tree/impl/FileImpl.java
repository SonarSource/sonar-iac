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

import java.util.List;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.File;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class FileImpl extends AbstractDockerTreeImpl implements File {
  private final Body body;
  private final SyntaxToken eof;

  public FileImpl(Body body, SyntaxToken eof) {
    this.body = body;
    this.eof = eof;
  }

  @Override
  public Body body() {
    return body;
  }

  @Override
  public List<Tree> children() {
    return List.of(body, eof);
  }

  @Override
  public Kind getKind() {
    return Kind.FILE;
  }
}
