/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.kubernetes.visitors;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.kubernetes.model.ProjectResource;

/**
 * Data class to provide information about the project. This allows to share cross-file knowledge to the individual checks.
 */
public final class ProjectContext {
  private final Map<String, Map<String, Set<ProjectResource>>> projectResourcePerNamespacePerPath = new HashMap<>();

  private ProjectContext() {
  }

  /**
   * Get all resources of a given {@code clazz} in a given {@code namespace} and that are accessible to a file with the given {@code path}.
   * This means that the resources can be in the same file, or in the same directory, or in the descendant directories, but not in the ancestor directories.<br/>
   * If the file is part of a Helm project, all files inside the project are accessible. The location of the Chart.yaml serves as the root directory of the project.
   */
  public Set<ProjectResource> getProjectResources(String namespace, InputFileContext inputFileContext, Class<? extends ProjectResource> clazz) {
    if (projectResourcePerNamespacePerPath.containsKey(namespace)) {
      var resourcesPerPath = projectResourcePerNamespacePerPath.get(namespace);

      var basePath = Optional.of(inputFileContext)
        .filter(HelmInputFileContext.class::isInstance)
        .map(it -> ((HelmInputFileContext) it).getHelmProjectDirectory())
        .or(() -> Optional.ofNullable(Path.of(inputFileContext.inputFile.uri()).getParent()))
        .map(Path::normalize)
        .map(Path::toUri)
        .map(URI::toString)
        .orElse("");

      return resourcesPerPath.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(basePath))
        .flatMap(entry -> entry.getValue().stream())
        .filter(clazz::isInstance)
        .collect(Collectors.toSet());
    }
    return Set.of();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private final ProjectContext ctx;

    private Builder() {
      this.ctx = new ProjectContext();
    }

    public Builder addResource(String namespace, String uri, ProjectResource resource) {
      ctx.projectResourcePerNamespacePerPath.computeIfAbsent(namespace, k -> new HashMap<>())
        .computeIfAbsent(uri, k -> new HashSet<>())
        .add(resource);
      return this;
    }

    public ProjectContext build() {
      return ctx;
    }
  }
}
