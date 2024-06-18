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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.iac.kubernetes.model.ProjectResource;

import static org.sonar.iac.helm.HelmFileSystem.retrieveHelmProjectFolder;

/**
 * Data class to provide information about the project. This allows to share cross-file knowledge to the individual checks.
 */
public final class ProjectContext {
  private final Map<String, Map<String, Set<ProjectResource>>> projectResourcePerNamespacePerPath = new HashMap<>();
  private final FileSystem fileSystem;

  private ProjectContext(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Get all resources of a given {@code clazz} in a given {@code namespace} and that are accessible to a file with the given {@code path}.
   * This means that the resources can be in the same file, or in the same directory, or in the descendant directories, but not in the ancestor directories.
   */
  public Set<ProjectResource> getProjectResources(String namespace, String path, Class<? extends ProjectResource> clazz) {
    if (projectResourcePerNamespacePerPath.containsKey(namespace)) {
      var resourcesPerPath = projectResourcePerNamespacePerPath.get(namespace);

      var filePath = Path.of(path);
      var basePath = Optional.ofNullable(retrieveHelmProjectFolder(filePath, fileSystem))
        .or(() -> Optional.ofNullable(filePath.getParent()))
        .map(Path::normalize)
        .map(Path::toUri)
        .map(URI::toString)
        .orElse("");

      return resourcesPerPath.entrySet().stream()
        .filter(entry -> basePath.isEmpty() || entry.getKey().startsWith(basePath))
        .flatMap(entry -> entry.getValue().stream())
        .filter(clazz::isInstance)
        .collect(Collectors.toSet());
    }
    return Set.of();
  }

  public static Builder builder(FileSystem fileSystem) {
    return new Builder(fileSystem);
  }

  public static class Builder {

    private final ProjectContext ctx;

    public Builder(FileSystem fileSystem) {
      this.ctx = new ProjectContext(fileSystem);
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
