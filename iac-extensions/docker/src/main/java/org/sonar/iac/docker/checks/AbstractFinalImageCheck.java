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
package org.sonar.iac.docker.checks;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.MultiStageBuildInspector;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.RunInstruction;

public abstract class AbstractFinalImageCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, this::checkBody);
  }

  private void checkBody(CheckContext ctx, Body body) {
    var multiStageBuildInspector = MultiStageBuildInspector.of(body);

    body.dockerImages().stream()
      .flatMap(image -> image.instructions().stream())
      .filter(instruction -> instruction.is(DockerTree.Kind.RUN))
      .map(RunInstruction.class::cast)
      .filter(multiStageBuildInspector::isInFinalImage)
      .forEach(runInstruction -> checkRunInstructionFromFinalImage(ctx, runInstruction));
  }

  protected abstract void checkRunInstructionFromFinalImage(CheckContext ctx, RunInstruction runInstruction);
}
