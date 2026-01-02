/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.docker.tree.api;

import org.sonar.iac.common.api.tree.Tree;

/**
 * Interface to define the contract of ExecForm, but with commands.
 * It is designed to be implemented with specific command tree, especially in Enterprise Edition to store a bash AST.
 */
public interface ExecFormCommand<T extends Tree> extends DockerTree {
  SyntaxToken leftBracket();

  T command();

  SyntaxToken rightBracket();
}
