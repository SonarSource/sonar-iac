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

import java.nio.file.Path;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;

public class ProjectContextEnricherVisitor extends TreeVisitor<InputFileContext> {
  private static final String DEFAULT_NAMESPACE = "";

  public ProjectContextEnricherVisitor(ProjectContext.Builder projectContextBuilder) {
    register(FileTree.class, (ctx, fileTree) -> handleFileTree(ctx, fileTree, projectContextBuilder));
  }

  private static void handleFileTree(InputFileContext ctx, FileTree fileTree, ProjectContext.Builder projectContextBuilder) {
    var uri = Path.of(ctx.inputFile.uri()).normalize().toUri();
    fileTree.documents().stream()
      .filter(MappingTree.class::isInstance)
      .map(MappingTree.class::cast)
      .forEach((MappingTree mappingTree) -> {
        var resource = ProjectResourceFactory.createResource(ctx.inputFile.relativePath(), mappingTree);
        if (resource == null) {
          return;
        }
        var namespace = getNamespace(mappingTree);
        projectContextBuilder.addResource(namespace, uri.toString(), resource);
      });
  }

  private static String getNamespace(MappingTree mappingTree) {
    return PropertyUtils.value(mappingTree, "metadata")
      .flatMap(metadata -> PropertyUtils.value(metadata, "namespace"))
      .filter(ScalarTree.class::isInstance)
      .map(it -> ((ScalarTree) it).value())
      .orElse(DEFAULT_NAMESPACE);
  }
}
