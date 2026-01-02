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

import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.Tree;

public interface ShellCode<T extends Tree> extends DockerTree {
  T code();

  /**
   * Returns the original source code for this shell code before preprocessing.
   * This is useful for checks that need to analyze the original line structure.
   *
   * @return the original source code, or null if not available (e.g., in Community edition)
   */
  @CheckForNull
  String originalSourceCode();
}
