/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
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

import static org.sonar.iac.docker.checks.utils.CheckUtils.ignoringHeredoc;

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
    init.register(RunInstruction.class, ignoringHeredoc(WorkdirInsteadCdCheck::checkCommandInstruction));
    init.register(CmdInstruction.class, WorkdirInsteadCdCheck::checkCommandInstruction);
    init.register(EntrypointInstruction.class, WorkdirInsteadCdCheck::checkCommandInstruction);
  }

  private static void checkCommandInstruction(CheckContext ctx, CommandInstruction commandInstruction) {
    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(commandInstruction);
    List<List<ArgumentResolution>> separatedArguments = ArgumentResolutionSplitter.splitCommands(argumentResolutions).elements();

    List<List<ArgumentResolution>> argumentsToCheck = takeFirstAndLastArgument(separatedArguments);

    for (List<ArgumentResolution> arguments : argumentsToCheck) {
      COMMAND_DETECTOR.searchWithoutSplit(arguments)
        .forEach(command -> ctx.reportIssue(command, MESSAGE));
    }
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
