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

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.model.Chart;
import org.sonar.iac.kubernetes.model.ProjectResource;

import static java.util.Objects.nonNull;

/**
 * Data class to provide information about the project. This allows to share cross-file knowledge to the individual checks.
 */
public final class ProjectContextImpl implements ProjectContext {

  private final Map<String, Map<String, Set<ProjectResource>>> projectResourcePerPathPerNamespace = new HashMap<>();
  private final Map<String, InputFileContext> inputFileContextPerPath = new HashMap<>();
  /**
   * The Chart of the project, if the project is a Helm project.
   */
  @Nullable
  private Chart chart;

  public ProjectContextImpl() {
    this.chart = null;
  }

  public void addResource(String namespace, String uri, ProjectResource resource) {
    projectResourcePerPathPerNamespace.computeIfAbsent(uri, k -> new HashMap<>())
      .computeIfAbsent(namespace, k -> new HashSet<>())
      .add(resource);
  }

  public void removeResource(String uri) {
    projectResourcePerPathPerNamespace.remove(uri);
  }

  public void addInputFileContext(String path, InputFileContext inputFileContext) {
    inputFileContextPerPath.put(path, inputFileContext);
  }

  public void setChart(@Nullable Chart chart) {
    this.chart = chart;
  }

  /**
   * Get all resources of a given {@code clazz} in a given {@code namespace} and that are accessible to a file in the given {@code inputFileContext}.
   * This means that the resources can be in the same file, or in the same directory, or in the descendant directories, but not in the ancestor directories.<br/>
   * If the file is part of a Helm project, all files inside the project are accessible. The location of the Chart.yaml serves as the root directory of the project.
   */
  @Override
  public <T extends ProjectResource> Set<T> getProjectResources(String namespace, InputFileContext inputFileContext, Class<T> clazz) {
    var basePath = Optional.of(inputFileContext)
      .filter(HelmInputFileContext.class::isInstance)
      .map(it -> ((HelmInputFileContext) it).getHelmProjectDirectory())
      .or(() -> Optional.ofNullable(Path.of(inputFileContext.inputFile.uri()).getParent()))
      .map(Path::normalize)
      .map(Path::toUri)
      .map(URI::toString)
      .orElse("");

    return projectResourcePerPathPerNamespace.entrySet().stream()
      .filter(entry -> entry.getKey().startsWith(basePath))
      .map(Map.Entry::getValue)
      .filter(entry -> nonNull(entry.get(namespace)))
      .flatMap(entry -> entry.get(namespace).stream())
      .filter(clazz::isInstance)
      .map(clazz::cast)
      .collect(Collectors.toSet());
  }

  @Nullable
  @Override
  public InputFileContext getInputFileContext(String path) {
    return inputFileContextPerPath.get(path);
  }

  @CheckForNull
  @Override
  public Chart getChart() {
    return chart;
  }
}
