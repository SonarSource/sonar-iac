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
package org.sonar.iac.docker.checks.utils;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.Instruction;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.impl.AliasImpl;
import org.sonar.iac.docker.tree.impl.ArgumentImpl;
import org.sonar.iac.docker.tree.impl.BodyImpl;
import org.sonar.iac.docker.tree.impl.DockerImageImpl;
import org.sonar.iac.docker.tree.impl.FromInstructionImpl;
import org.sonar.iac.docker.tree.impl.LiteralImpl;
import org.sonar.iac.docker.tree.impl.RunInstructionImpl;
import org.sonar.iac.docker.tree.impl.SingleArgumentListImpl;
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
    var baseInstruction = createRunInstruction();
    var baseStage = createDockerImage("ubuntu:latest", "base", baseInstruction);
    var buildInstruction = createRunInstruction();
    var buildStage = createDockerImage("base", "build", buildInstruction);
    var otherInstruction = createRunInstruction();
    var otherStage = createDockerImage("build", "other", otherInstruction);
    var finalInstruction = createRunInstruction();
    var finalStage = createDockerImage("build", "final", finalInstruction);
    var body = createBody(buildStage, baseStage, otherStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isInFinalImage(baseInstruction)).isTrue();
    assertThat(inspector.isInFinalImage(buildInstruction)).isTrue();
    assertThat(inspector.isInFinalImage(otherInstruction)).isFalse();
    assertThat(inspector.isInFinalImage(finalInstruction)).isTrue();
  }

  @Test
  void shouldDetectInstructionsInFinalImageWithoutFinalAlias() {
    var buildInstruction = createRunInstruction();
    var buildStage = createDockerImage("ubuntu:latest", "build", buildInstruction);
    var finalInstruction = createRunInstruction();
    var finalStage = createDockerImage("build", null, finalInstruction);
    var body = createBody(buildStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isInFinalImage(buildInstruction)).isTrue();
    assertThat(inspector.isInFinalImage(finalInstruction)).isTrue();
  }

  @Test
  void shouldDetectInstructionsInFinalImageWithCircularDependencies() {
    var buildInstruction = createRunInstruction();
    var buildStage = createDockerImage("final", "build", buildInstruction);
    var finalInstruction = createRunInstruction();
    var finalStage = createDockerImage("build", "final", finalInstruction);
    var body = createBody(buildStage, finalStage);
    var inspector = MultiStageBuildInspector.of(body);

    assertThat(inspector.isInFinalImage(buildInstruction)).isTrue();
    assertThat(inspector.isInFinalImage(finalInstruction)).isTrue();
  }

  private Body createBody(DockerImage... dockerImages) {
    var body = new BodyImpl(List.of(), List.of(dockerImages));
    Arrays.stream(dockerImages).forEach(dockerImage -> dockerImage.setParent(body));
    return body;
  }

  private static DockerImage createDockerImage(String imageName, @Nullable String aliasName) {
    return createDockerImage(imageName, aliasName, createRunInstruction());
  }

  private static DockerImage createDockerImage(String imageName, @Nullable String aliasName, Instruction instruction) {
    var imageArgument = new ArgumentImpl(List.of(new LiteralImpl(new SyntaxTokenImpl(imageName, null, null))));
    var alias = aliasName != null ? new AliasImpl(null, new SyntaxTokenImpl(aliasName, null, null)) : null;
    var fromInstruction = new FromInstructionImpl(null, null, imageArgument, alias);
    var image = new DockerImageImpl(fromInstruction, List.of(instruction));
    instruction.setParent(image);
    return image;
  }

  private static RunInstruction createRunInstruction() {
    var command = "apt-get install nginx";
    var commandLiteral = new LiteralImpl(new SyntaxTokenImpl(command, null, null));
    var commandArgument = new ArgumentImpl(List.of(commandLiteral));
    return new RunInstructionImpl(null, List.of(), new SingleArgumentListImpl(commandArgument));
  }
}
