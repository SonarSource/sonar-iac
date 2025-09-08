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
package org.sonar.iac.kubernetes.visitors;

import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.model.Chart;
import org.sonar.iac.kubernetes.model.ProjectResource;

public interface ProjectContext {

  /**
   * Get all resources of a given {@code clazz} in a given {@code namespace} and that are accessible to a file in the given {@code inputFileContext}.
   * This means that the resources can be in the same file, or in the same directory, or in the descendant directories, but not in the ancestor directories.<br/>
   * If the file is part of a Helm project, all files inside the project are accessible. The location of the Chart.yaml serves as the root directory of the project.
   */
  <T extends ProjectResource> Set<T> getNamespaceProjectResources(String namespace, InputFileContext inputFileContext, Class<T> clazz);

  /**
   * Similar to the method {@link #getNamespaceProjectResources(String, InputFileContext, Class)}, but it looks into any namespace.
   */
  <T extends ProjectResource> Set<T> getProjectResources(InputFileContext inputFileContext, Class<T> clazz);

  @Nullable
  InputFileContext getInputFileContext(String path);

  @CheckForNull
  Chart getChart();
}
