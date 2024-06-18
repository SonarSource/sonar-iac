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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.iac.kubernetes.model.ProjectResource;

/**
 * Data class to provide information about the project. This allows to share cross-file knowledge to the individual checks.
 */
public final class ProjectContext {
  private final Map<String, Map<String, Set<ProjectResource>>> projectResourcePerNamespacePerPath = new HashMap<>();

  private ProjectContext() {
  }

  public Set<ProjectResource> getProjectResource(String namespace, String path, Class<? extends ProjectResource> clazz) {
    if (projectResourcePerNamespacePerPath.containsKey(namespace)) {
      var resourcesPerPath = projectResourcePerNamespacePerPath.get(namespace);
      if (resourcesPerPath.containsKey(path)) {
        var resources = resourcesPerPath.get(path);
        return resources.stream()
          .filter(clazz::isInstance)
          .collect(Collectors.toSet());
      }
    }
    return Set.of();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final ProjectContext ctx;

    public Builder() {
      this.ctx = new ProjectContext();
    }

    public Builder addResource(String namespace, String path, ProjectResource resource) {
      ctx.projectResourcePerNamespacePerPath.computeIfAbsent(namespace, k -> new HashMap<>())
        .computeIfAbsent(path, k -> new HashSet<>())
        .add(resource);
      return this;
    }

    public ProjectContext build() {
      return ctx;
    }
  }
}
