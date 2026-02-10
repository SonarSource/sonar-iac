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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.FromInstruction;

import static org.sonar.iac.docker.checks.utils.CheckUtils.getImageDigest;
import static org.sonar.iac.docker.checks.utils.CheckUtils.getImageTag;

@Rule(key = "S8431")
public class CombinedTagAndDigestCheck implements IacCheck {

  private static final String MESSAGE = "Use either the version tag or the digest for the image instead of both.";

  @Override
  public void initialize(InitContext init) {
    init.register(FromInstruction.class, CombinedTagAndDigestCheck::checkFromInstruction);
  }

  private static void checkFromInstruction(CheckContext ctx, FromInstruction fromInstruction) {
    var resolvedImage = ArgumentResolution.of(fromInstruction.image());
    String fullImageName = resolvedImage.value();
    if (isTagAndDigestCombined(fullImageName)) {
      ctx.reportIssue(fromInstruction.image().textRange(), MESSAGE);
    }
  }

  private static boolean isTagAndDigestCombined(String fullImageName) {
    return getImageTag(fullImageName).isPresent() && getImageDigest(fullImageName).isPresent();
  }
}
