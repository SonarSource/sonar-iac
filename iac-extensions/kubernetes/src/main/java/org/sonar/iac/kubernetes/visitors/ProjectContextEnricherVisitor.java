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

import java.nio.file.Path;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.common.yaml.tree.FileTree;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.kubernetes.model.Chart;

public class ProjectContextEnricherVisitor extends TreeVisitor<InputFileContext> {
  private static final String DEFAULT_NAMESPACE = "";

  public ProjectContextEnricherVisitor(ProjectContextImpl projectContextImpl) {
    register(FileTree.class, (ctx, fileTree) -> handleFileTree(ctx, fileTree, projectContextImpl));
  }

  private static void handleFileTree(InputFileContext ctx, FileTree fileTree, ProjectContextImpl projectContextImpl) {
    projectContextImpl.addInputFileContext(ctx.inputFile.relativePath(), ctx);
    if ("Chart.yaml".equals(ctx.inputFile.filename())) {
      projectContextImpl.setChart(Chart.fromFileTree(fileTree));
      return;
    }

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
        projectContextImpl.addResource(namespace, uri.toString(), resource);
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
