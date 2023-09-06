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

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6500")
public class PackageInstallationCheck implements IacCheck {

  private static final String MESSAGE = "Make sure automatically installing recommended packages is safe here.";

  private static final Set<String> APT_COMMANDS = Set.of("apt", "apt-get");
  private static final CommandDetector SENSITIVE_APT_COMMAND = CommandDetector.builder()
    .with(APT_COMMANDS::contains)
    .withAnyFlag()
    .with("install"::equals)
    .withAnyFlagExcept("--no-install-recommends")
    .build();
  private static final CommandDetector SENSITIVE_APTITUDE_COMMAND = CommandDetector.builder()
    .with("aptitude"::equals)
    .withAnyFlag()
    .with("install"::equals)
    .withAnyFlagExcept("--without-recommends")
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageInstallationCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<ArgumentResolution> resolvedArgument = CheckUtils.resolveInstructionArguments(runInstruction);

    SENSITIVE_APT_COMMAND.searchWithSplit(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
    SENSITIVE_APTITUDE_COMMAND.searchWithSplit(resolvedArgument).forEach(command -> ctx.reportIssue(command, MESSAGE));
  }
}
