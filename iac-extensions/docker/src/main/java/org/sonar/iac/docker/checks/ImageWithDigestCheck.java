/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.FromInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

@Rule(key = "S6497")
public class ImageWithDigestCheck implements IacCheck {

  private static final String MESSAGE = "Setting a digest will prevent receiving updates of the base image. Make sure it is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(FromInstruction.class, ImageWithDigestCheck::checkFrom);
  }

  private static void checkFrom(CheckContext ctx, FromInstruction fromInstruction) {
    String image = ArgumentUtils.resolve(fromInstruction.image()).value();
    if (image != null && image.contains("@")) {
      ctx.reportIssue(fromInstruction.image(), MESSAGE);
    }
  }
}
