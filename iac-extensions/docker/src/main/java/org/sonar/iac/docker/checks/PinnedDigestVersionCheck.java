/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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

import static org.sonar.iac.docker.checks.utils.CheckUtils.isScratchImage;

@Rule(key = "S7023")
public class PinnedDigestVersionCheck implements IacCheck {

  private static final String MESSAGE = "Add digest to this tag to pin the version of the base image.";

  @Override
  public void initialize(InitContext init) {
    init.register(FromInstruction.class, PinnedDigestVersionCheck::checkFromInstruction);
  }

  private static void checkFromInstruction(CheckContext ctx, FromInstruction fromInstruction) {
    var resolvedImage = ArgumentResolution.of(fromInstruction.image());
    if (resolvedImage.isResolved()) {
      String fullImageName = resolvedImage.value();

      if (!isScratchImage(fullImageName) && !isMalformedOrLatestVersion(fullImageName) && !hasPinnedDigest(fullImageName)) {
        ctx.reportIssue(fromInstruction.image().textRange(), MESSAGE);
      }
    }
  }

  // We don't want to raise on malformed image names
  // We don't want to raise on latest version because SpecificVersionTagCheck would already raise an issue here
  private static boolean isMalformedOrLatestVersion(String fullImageName) {
    String[] splitImageName = fullImageName.split(":");
    if (splitImageName.length <= 1) {
      return true;
    } else {
      return splitImageName[0].isBlank() || "latest".equals(splitImageName[1]);
    }
  }

  private static boolean hasPinnedDigest(String fullImageName) {
    return fullImageName.contains("@");
  }
}
