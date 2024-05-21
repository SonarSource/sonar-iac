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
    var alias = fromInstruction.alias();
    if (alias != null) {
      encounteredAlias.add(alias.alias().value());
    }

    var resolvedImage = ArgumentResolution.of(fromInstruction.image());
    if (resolvedImage.isUnresolved()) {
      return;
    }
    String fullImageName = resolvedImage.value();

    if (isScratchImage(fullImageName)) {
      return;
    }

    if (hasSensitiveVersionTag(fullImageName) && !encounteredAlias.contains(fullImageName)) {
      ctx.reportIssue(fromInstruction.image().textRange(), MESSAGE);
    }
  }

  private static boolean hasSensitiveVersionTag(String fullImageName) {
    if (fullImageName.contains("@")) {
      return false;
    } else if (fullImageName.contains(":")) {
      // raise an issue if the version tag is "latest"
      String[] splitImageName = fullImageName.split(":");
      return splitImageName.length > 1 && "latest".equals(splitImageName[1]);
    } else {
      // no version tag specified, docker assumes "latest"
      return true;
    }
  }
}
