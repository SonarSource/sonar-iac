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
package org.sonar.iac.docker.checks.utils;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.impl.AliasImpl;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.BodyImpl;
import org.sonar.iac.docker.tree.impl.DockerImageImpl;
import org.sonar.iac.docker.tree.impl.FromInstructionImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.SyntaxTokenImpl;

import static org.assertj.core.api.Assertions.assertThat;

class MultiStageBuildInspectorTest {

  @Test
  void shouldDetectLastStage() {
    var buildStage = createDockerImage("ubuntu:latest", "build");
    var finalStage = createDockerImage("build", "final");
    createBody(buildStage, finalStage); // Needed to set the parent

    assertThat(MultiStageBuildInspector.isLastStage(buildStage)).isFalse();
    assertThat(MultiStageBuildInspector.isLastStage(finalStage)).isTrue();
  }

  @Test
  void shouldDetectInstructionsInFinalImage() {
    var baseStage = createDockerImage("ubuntu:latest", "base");
    var buildStage = createDockerImage("base", "build");
    var otherStage = createDockerImage("build", "other");
    var finalStage = createDockerImage("build", "final");
    var body = createBody(buildStage, baseStage, otherStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isStageInFinalImage(baseStage)).isTrue();
    assertThat(inspector.isStageInFinalImage(buildStage)).isTrue();
    assertThat(inspector.isStageInFinalImage(otherStage)).isFalse();
    assertThat(inspector.isStageInFinalImage(finalStage)).isTrue();
  }

  @Test
  void shouldDetectInstructionsInFinalImageWithoutFinalAlias() {
    var buildStage = createDockerImage("ubuntu:latest", "build");
    var finalStage = createDockerImage("build", null);
    var body = createBody(buildStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isStageInFinalImage(buildStage)).isTrue();
    assertThat(inspector.isStageInFinalImage(finalStage)).isTrue();
  }

  @Test
  void shouldDetectInstructionsInFinalImageWithCircularDependencies() {
    var buildStage = createDockerImage("final", "build");
    var finalStage = createDockerImage("build", "final");
    var body = createBody(buildStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isStageInFinalImage(buildStage)).isTrue();
    assertThat(inspector.isStageInFinalImage(finalStage)).isTrue();
  }

  private Body createBody(DockerImage... dockerImages) {
    var body = new BodyImpl(List.of(), List.of(dockerImages));
    Arrays.stream(dockerImages).forEach(dockerImage -> dockerImage.setParent(body));
    return body;
  }

  private static DockerImage createDockerImage(String imageName, @Nullable String aliasName) {
    var imageArgument = new ArgumentImpl(List.of(new LiteralImpl(new SyntaxTokenImpl(imageName, null, null))));
    var alias = aliasName != null ? new AliasImpl(null, new SyntaxTokenImpl(aliasName, null, null)) : null;
    var fromInstruction = new FromInstructionImpl(null, null, imageArgument, alias);
    return new DockerImageImpl(fromInstruction, List.of());
  }
}
