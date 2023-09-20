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

import java.util.ArrayList;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6597")
public class WorkdirInsteadCdCheck implements IacCheck {
  private static final String MESSAGE = "WORKDIR instruction should be used instead of cd command.";
  private static final CommandDetector COMMAND_DETECTOR = CommandDetector.builder()
    .with("cd")
    .withAnyFlag()
    .with(e -> true)
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, WorkdirInsteadCdCheck::checkRunInstruction);
    init.register(CmdInstruction.class, WorkdirInsteadCdCheck::checkGeneralCommandInstruction);
    init.register(EntrypointInstruction.class, WorkdirInsteadCdCheck::checkGeneralCommandInstruction);
  }

  private static void checkGeneralCommandInstruction(CheckContext ctx, CommandInstruction commandInstruction) {
    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(commandInstruction);
    List<List<ArgumentResolution>> separatedArguments = ArgumentResolutionSplitter.splitCommands(argumentResolutions).elements();

    List<List<ArgumentResolution>> argumentsToCheck = takeFirstAndLastArgument(separatedArguments);

    for (List<ArgumentResolution> arguments : argumentsToCheck) {
      COMMAND_DETECTOR.searchWithoutSplit(arguments)
        .forEach(command -> ctx.reportIssue(command, MESSAGE));
    }
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    if (runInstruction.containsHeredoc()) {
      return;
    }
    checkGeneralCommandInstruction(ctx, runInstruction);
  }

  private static List<List<ArgumentResolution>> takeFirstAndLastArgument(List<List<ArgumentResolution>> elements) {
    List<List<ArgumentResolution>> toCheck;
    if (elements.size() == 1) {
      toCheck = elements;
    } else {
      toCheck = new ArrayList<>();
      toCheck.add(elements.get(0));
      toCheck.add(elements.get(elements.size() - 1));
    }
    return toCheck;
  }
}
