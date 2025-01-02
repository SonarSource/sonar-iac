/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.model;

import javax.annotation.CheckForNull;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.yaml.tree.FileTree;

public record Chart(String apiVersion) {
  @CheckForNull
  public static Chart fromFileTree(FileTree fileTree) {
    return fileTree.documents().stream().findFirst()
      .flatMap(tree -> PropertyUtils.value(tree, "apiVersion", TextTree.class))
      .map(TextTree::value)
      .map(Chart::new)
      .orElse(null);
  }
}
