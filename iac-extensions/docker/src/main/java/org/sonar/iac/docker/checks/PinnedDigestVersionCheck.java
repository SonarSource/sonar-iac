/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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
import org.sonar.iac.common.checks.DockerImageReference;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.FromInstruction;

@Rule(key = "S7023")
public class PinnedDigestVersionCheck implements IacCheck {

  private static final String MESSAGE = "Add digest to this tag to pin the version of the base image.";

  @Override
  public void initialize(InitContext init) {
    init.register(FromInstruction.class, PinnedDigestVersionCheck::checkFromInstruction);
  }

  private static void checkFromInstruction(CheckContext ctx, FromInstruction fromInstruction) {
    var resolvedImage = ArgumentResolution.of(fromInstruction.image());
    if (!resolvedImage.isResolved()) {
      return;
    }
    DockerImageReference.parse(resolvedImage.value())
      .filter(image -> !image.isScratch() && needsPinnedDigest(image))
      .ifPresent(image -> ctx.reportIssue(fromInstruction.image().textRange(), MESSAGE));
  }

  // We don't want to raise on a blank/absent tag, or "latest" (SpecificVersionTagCheck already raises for those).
  private static boolean needsPinnedDigest(DockerImageReference image) {
    return image.hasSpecificVersion() && image.digest() == null;
  }
}
