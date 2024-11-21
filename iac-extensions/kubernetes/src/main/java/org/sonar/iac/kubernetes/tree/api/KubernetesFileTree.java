/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.tree.api;

import javax.annotation.CheckForNull;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.helm.tree.api.GoTemplateTree;

/**
 * A custom YAML file tree representing a Kubernetes file.
 * If the file is a Helm file, the AST of Go template is also available.
 */
public interface KubernetesFileTree extends FileTree {
  @CheckForNull
  GoTemplateTree getGoTemplateAst();
}
