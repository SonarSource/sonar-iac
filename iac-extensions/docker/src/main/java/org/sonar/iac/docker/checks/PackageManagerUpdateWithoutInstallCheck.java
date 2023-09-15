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
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6595")
public class PackageManagerUpdateWithoutInstallCheck implements IacCheck {
  private static final String MESSAGE = "Update cache and install packages in single RUN instruction.";
  private static final CommandDetector PACKAGE_MANAGER_DETECTOR = CommandDetector.builder()
    .with(Set.of("apk", "apt", "apt-get", "aptitude"))
    .withOptionalRepeating(s -> true)
    .build();
  private static final CommandDetector PACKAGE_MANAGER_UPDATE_DETECTOR = CommandDetector.builder()
    .with(Set.of("apk", "apt", "apt-get", "aptitude"))
    .withAnyFlag()
    // all currently supported programs have the same command for update
    .with("update")
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageManagerUpdateWithoutInstallCheck::checkPackageManagerInvocations);
  }

  private static void checkPackageManagerInvocations(CheckContext ctx, RunInstruction runInstruction) {
    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(CheckUtils.resolveInstructionArguments(runInstruction));
    for (int i = 0; i < splitCommands.elements().size(); i++) {
      List<ArgumentResolution> argumentResolutions = splitCommands.elements().get(i);
      for (CommandDetector.Command command : PACKAGE_MANAGER_UPDATE_DETECTOR.search(argumentResolutions)) {
        List<List<ArgumentResolution>> commandsAfterMatch = splitCommands.elements().subList(i + 1, splitCommands.elements().size());
        commandsAfterMatch.add(0, argumentResolutions.subList(
          argumentResolutions.indexOf(command.getResolvedArguments().get(command.getResolvedArguments().size() - 1)) + 1,
          argumentResolutions.size()));
        // There may be various commands that would require updated indices, so we only check presence for absence of another invocation
        boolean hasNoPackageManagerInvocation = commandsAfterMatch.stream().allMatch(resolutions -> PACKAGE_MANAGER_DETECTOR.search(resolutions).isEmpty());
        if (hasNoPackageManagerInvocation) {
          ctx.reportIssue(command.textRange(), MESSAGE);
        }
      }
    }

  }
}
