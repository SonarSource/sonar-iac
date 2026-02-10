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
package org.sonar.iac.docker.checks;

import java.util.HashSet;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Body;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.sonar.iac.docker.checks.utils.CheckUtils.getImageDigest;
import static org.sonar.iac.docker.checks.utils.CheckUtils.getImageTag;
import static org.sonar.iac.docker.checks.utils.CheckUtils.isScratchImage;

@Rule(key = "S6596")
public class SpecificVersionTagCheck implements IacCheck {

  private static final String MESSAGE = "Use a specific version tag for the image.";

  @Override
  public void initialize(InitContext init) {
    init.register(Body.class, SpecificVersionTagCheck::checkBody);
  }

  private static void checkBody(CheckContext ctx, Body body) {
    Set<String> encounteredAlias = new HashSet<>();
    for (DockerImage dockerImage : body.dockerImages()) {
      checkFromInstruction(ctx, dockerImage.from(), encounteredAlias);
    }
  }

  private static void checkFromInstruction(CheckContext ctx, FromInstruction fromInstruction, Set<String> encounteredAlias) {
    var resolvedImage = ArgumentResolution.of(fromInstruction.image());
    if (resolvedImage.isResolved()) {
      String fullImageName = resolvedImage.value();

      if (!isScratchImage(fullImageName) && hasSensitiveVersionTag(fullImageName) && !encounteredAlias.contains(fullImageName)) {
        ctx.reportIssue(fromInstruction.image().textRange(), MESSAGE);
      }
    }

    var alias = fromInstruction.alias();
    if (alias != null) {
      encounteredAlias.add(alias.alias().value());
    }
  }

  private static boolean hasSensitiveVersionTag(String fullImageName) {
    if (getImageDigest(fullImageName).isPresent()) {
      return false;
    }
    // return true, if image tag is absent or equals to latest
    return getImageTag(fullImageName).map("latest"::equals).orElse(true);
  }
}
