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

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S7018")
public class SortedArgumentListCheck implements IacCheck {
  private static final String MESSAGE = "Sort these package names alphanumerically.";
  private static final String INSTALL_COMMAND = "install";
  private static final String ADD_COMMAND = "add";
  private static final int MINIMAL_LENGTH_TO_SORT = 3;

  private static final CommandDetector DEBIAN_PACKAGE_MANAGER_DETECTOR = CommandDetector.builder()
    .with(Set.of("apt", "apt-get", "aptitude"))
    .withAnyFlag()
    .with(INSTALL_COMMAND)
    .withAnyFlag()
    .withOptionalRepeating(s -> true)
    .build();
  private static final CommandDetector APK_DETECTOR = CommandDetector.builder()
    .with("apk")
    .withAnyFlag()
    .with(ADD_COMMAND)
    .withAnyFlag()
    .withOptionalRepeating(s -> true)
    .build();
  private static final CommandDetector PIP_DETECTOR = CommandDetector.builder()
    .with("pip")
    .withAnyFlag()
    .with(INSTALL_COMMAND)
    // don't sort arguments if a file is used
    .withAnyFlagExcept("-r", "--requirement")
    .withOptionalRepeating(s -> true)
    .build();
  private static final Set<CommandDetector> COMMAND_DETECTORS = Set.of(DEBIAN_PACKAGE_MANAGER_DETECTOR, APK_DETECTOR, PIP_DETECTOR);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, SortedArgumentListCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction runInstruction) {
    if (runInstruction.getKindOfArgumentList() == DockerTree.Kind.HEREDOCUMENT) {
      // TODO SONARIAC-1557 Heredoc should be treated as multiple instructions
      // Otherwise, it's not clear where to stop the matcher, and the next command can be captured.
      return;
    }

    var argumentResolutions = runInstruction.arguments().stream().map(ArgumentResolution::of).toList();
    COMMAND_DETECTORS.stream()
      .map(it -> it.search(argumentResolutions))
      .filter(it -> !it.isEmpty())
      .flatMap(List::stream)
      .forEach(command -> checkInstallationCommand(ctx, command));
  }

  private static void checkInstallationCommand(CheckContext ctx, CommandDetector.Command command) {
    var installationCommand = switch (command.getResolvedArguments().get(0).value()) {
      case "apk" -> ADD_COMMAND;
      default -> INSTALL_COMMAND;
    };
    var argumentsToCheck = afterLastFlagAndCommand(command.getResolvedArguments(), installationCommand).stream()
      .map(ArgumentResolution::value)
      .toList();
    if (argumentsToCheck.size() >= MINIMAL_LENGTH_TO_SORT && !isSorted(argumentsToCheck)) {
      ctx.reportIssue(command, MESSAGE);
    }
  }

  private static List<ArgumentResolution> afterLastFlagAndCommand(List<ArgumentResolution> argumentResolutions, String installationCommand) {
    var flags = argumentResolutions.stream().filter(it -> it.value().startsWith("-")).toList();
    int lastFlagIndex;
    if (flags.isEmpty()) {
      lastFlagIndex = 0;
    } else {
      lastFlagIndex = argumentResolutions.indexOf(flags.get(flags.size() - 1));
    }
    var commandIndex = argumentResolutions.indexOf(argumentResolutions.stream().filter(it -> it.value().equals(installationCommand)).findFirst().orElse(null));
    return argumentResolutions.subList(Math.max(lastFlagIndex, commandIndex) + 1, argumentResolutions.size());
  }

  private static boolean isSorted(List<String> arguments) {
    for (var i = 0; i < arguments.size() - 1; i++) {
      if (arguments.get(i).compareTo(arguments.get(i + 1)) > 0) {
        return false;
      }
    }
    return true;
  }
}
