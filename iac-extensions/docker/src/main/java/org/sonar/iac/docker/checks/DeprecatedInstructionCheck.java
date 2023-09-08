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
import org.sonar.iac.docker.tree.api.MaintainerInstruction;

@Rule(key = "S6586")
public class DeprecatedInstructionCheck implements IacCheck {

  private static final String MESSAGE = "Replace deprecated instructions with an up-to-date equivalent.";

  @Override
  public void initialize(InitContext init) {
    init.register(MaintainerInstruction.class, DeprecatedInstructionCheck::checkMaintainerInstruction);
  }

  private static void checkMaintainerInstruction(CheckContext ctx, MaintainerInstruction maintainerInstruction) {
    ctx.reportIssue(maintainerInstruction, MESSAGE);
  }
}
