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
package org.sonar.iac.docker.checks.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.sonar.iac.common.extension.visitors.TreeVisitor;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Alias;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public final class MultiStageBuildInspector {

  private final StageDependencyGraph stageDependencyGraph;

  private MultiStageBuildInspector(StageDependencyGraph stageDependencyGraph) {
    this.stageDependencyGraph = stageDependencyGraph;
  }

  /**
   * Return a new instance of {@link MultiStageBuildInspector} for the given {@link Body}.
   * This entrypoint performs the analysis of the given {@link Body} to build the dependency graph itself and does not rely on the {@link TreeVisitor} system.
   */
  public static MultiStageBuildInspector of(Body body) {
    var stageDependencyGraph = StageDependencyGraph.of(body);
    return new MultiStageBuildInspector(stageDependencyGraph);
  }

  public static boolean isLastStage(DockerImage dockerImage) {
    var body = (Body) dockerImage.parent();
    return dockerImage == getLastStage(body);
  }

  public boolean isStageInFinalImage(DockerImage dockerImage) {
    return isLastStage(dockerImage) || isStageDependencyOfFinalStage(dockerImage);
  }

  private boolean isStageDependencyOfFinalStage(DockerImage stage) {
    var lastStageDependencies = stageDependencyGraph.getLastStageDependencies();
    return getStageName(stage)
      .map(lastStageDependencies::contains)
      .orElse(false);
  }

  public Set<String> getStageDependencies(String stage) {
    return stageDependencyGraph.getStageDependencies(stage, new HashSet<>());
  }

  private static DockerImage getLastStage(Body body) {
    var dockerImages = body.dockerImages();
    return dockerImages.get(dockerImages.size() - 1);
  }

  private static Optional<String> getStageName(DockerImage dockerImage) {
    var imageName = dockerImage.from().alias();
    return Optional.ofNullable(imageName)
      .map(Alias::alias)
      .map(SyntaxToken::value);
  }

  private static final class StageDependencyGraph {
    private final Map<String, String> graph = new HashMap<>();
    private final Body body;

    private StageDependencyGraph(Body body) {
      this.body = body;
    }

    private static StageDependencyGraph of(Body body) {
      var stageDependencyGraph = new StageDependencyGraph(body);
      body.dockerImages().forEach(stageDependencyGraph::addStage);
      return stageDependencyGraph;
    }

    private Set<String> getLastStageDependencies() {
      var lastStage = getLastStage(body);
      var lastStageName = getStageName(lastStage);
      if (lastStageName.isPresent()) {
        return getStageDependencies(lastStageName.get(), new HashSet<>());
      }
      var lastStageBaseImage = getStageBaseImageName(lastStage);
      var dependencies = new HashSet<String>();
      dependencies.add(lastStageBaseImage);
      dependencies.addAll(getStageDependencies(lastStageBaseImage, new HashSet<>()));
      return dependencies;
    }

    private Set<String> getStageDependencies(String stage, Set<String> visited) {
      if (!graph.containsKey(stage)) {
        return Set.of();
      }
      visited.add(stage);
      String baseImage = graph.get(stage);
      if (visited.contains(baseImage)) {
        // This is used to avoid infinite recursive calls in case of circular dependencies
        return Set.of();
      }
      var dependencies = new HashSet<String>();
      dependencies.add(baseImage);
      dependencies.addAll(getStageDependencies(baseImage, visited));
      return dependencies;
    }

    private void addStage(DockerImage dockerImage) {
      getStageName(dockerImage)
        .ifPresent(alias -> graph.put(alias, getStageBaseImageName(dockerImage)));
    }

    private static String getStageBaseImageName(DockerImage dockerImage) {
      Argument baseImage = dockerImage.from().image();
      return ArgumentResolution.of(baseImage).value();
    }
  }
}
