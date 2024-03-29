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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Rule(key = "S6505")
public class PackageInstallationScriptExecutionCheck implements IacCheck {

  private static final String MESSAGE = "Omitting --ignore-scripts can lead to the execution of shell scripts. Make sure it is safe here.";

  private static final String REQUIRED_FLAG = "--ignore-scripts";
  private static final Set<String> NPM_COMMAND = Set.of("npm", "pnpm");
  private static final Set<String> NPM_INSTALL_COMMAND = Set.of("install", "ci", "add", "i", "in", "ins", "inst", "insta", "instal", "isnt", "isnta", "isntal", "isntall");

  private static final CommandDetector NPM_PACKAGE_INSTALLATION = CommandDetector.builder()
    .with(NPM_COMMAND)
    .with(NPM_INSTALL_COMMAND)
    .withAnyFlagExcept(REQUIRED_FLAG)
    .build();

  private static final CommandDetector YARN_PACKAGE_INSTALL = CommandDetector.builder()
    .with("yarn")
    .with("install")
    .withAnyFlagExcept(REQUIRED_FLAG)
    .build();

  // https://classic.yarnpkg.com/en/docs/cli/#toc-default-command: `yarn` invocation without any command is equivalent to `yarn install`
  private static final CommandDetector YARN_PACKAGE_DEFAULT = CommandDetector.builder()
    .with("yarn")
    .withAnyFlagExcept(REQUIRED_FLAG)
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageInstallationScriptExecutionCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    List<List<ArgumentResolution>> resolvedArguments = ArgumentResolutionSplitter.splitCommands(CheckUtils.resolveInstructionArguments(runInstruction)).elements();
    List<CommandDetector.Command> sensitiveCommands = new ArrayList<>();

    for (List<ArgumentResolution> resolvedArgument : resolvedArguments) {
      sensitiveCommands.addAll(NPM_PACKAGE_INSTALLATION.search(resolvedArgument));
      sensitiveCommands.addAll(YARN_PACKAGE_INSTALL.search(resolvedArgument));
      sensitiveCommands.addAll(YARN_PACKAGE_DEFAULT.search(resolvedArgument).stream()
        // matched starting with the start of the input
        .filter(c -> c.getResolvedArguments().get(0).equals(resolvedArgument.get(0)))
        // matched till the end of input
        .filter(c -> c.getResolvedArguments().get(c.getResolvedArguments().size() - 1).equals(resolvedArgument.get(resolvedArgument.size() - 1)))
        .toList());
    }

    sensitiveCommands.forEach(command -> ctx.reportIssue(command, MESSAGE));
  }

}
