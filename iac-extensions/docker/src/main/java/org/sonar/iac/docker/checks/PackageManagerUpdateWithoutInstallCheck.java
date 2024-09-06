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
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.ArgumentResolutionSplitter;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.checks.utils.command.SeparatedList;
import org.sonar.iac.docker.parser.DockerParser;
import org.sonar.iac.docker.parser.grammar.DockerLexicalGrammar;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.ShellForm;
import org.sonar.iac.docker.tree.api.Variable;

@Rule(key = "S6595")
public class PackageManagerUpdateWithoutInstallCheck implements IacCheck {
  private static final DockerParser RESOLVED_COMMAND_PART_PARSER = DockerParser.create(DockerLexicalGrammar.SHELL_FORM_GENERIC);
  private static final String MESSAGE = "Update cache and install packages in single RUN instruction.";
  private static final Set<String> PACKAGE_MANAGERS = Set.of("apk", "apt", "apt-get", "aptitude");
  private static final CommandDetector PACKAGE_MANAGER_DETECTOR = CommandDetector.builder()
    .with(PACKAGE_MANAGERS)
    .withAnyIncludingUnresolvedRepeating(s -> true)
    .build();
  private static final CommandDetector PACKAGE_MANAGER_UPDATE_DETECTOR = CommandDetector.builder()
    .with(PACKAGE_MANAGERS)
    .withAnyFlag()
    // all currently supported programs have the same command for update
    .with("update")
    .build();

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageManagerUpdateWithoutInstallCheck::checkPackageManagerInvocations);
  }

  private static void checkPackageManagerInvocations(CheckContext ctx, RunInstruction runInstruction) {
    var arguments = CheckUtils.resolveInstructionArguments(runInstruction).stream()
      .flatMap((ArgumentResolution argument) -> {
        var expressions = argument.argument().expressions();
        if (argument.isResolved() && expressions.stream().anyMatch(Variable.class::isInstance)) {
          // If the argument represents a resolved variable, it may in fact be a part of the command and not just a single value.
          var shellForm = (ShellForm) RESOLVED_COMMAND_PART_PARSER.parse(" " + argument.value());
          return shellForm.arguments().stream().map(ArgumentResolution::ofWithoutStrippingQuotes);
        } else {
          return Stream.of(argument);
        }
      }).toList();
    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(arguments);
    for (var i = 0; i < splitCommands.elements().size(); i++) {
      List<ArgumentResolution> argumentResolutions = splitCommands.elements().get(i);
      for (CommandDetector.Command command : PACKAGE_MANAGER_UPDATE_DETECTOR.search(argumentResolutions)) {
        List<List<ArgumentResolution>> commandsAfterMatch = splitCommands.elements().subList(i + 1, splitCommands.elements().size());
        // In some cases, e.g. in HEREDOC, `ArgumentSplitter` doesn't split all the sub-commands.
        // To account for this, we also need to analyze the part of the current command the region matched by CommandDetector.
        commandsAfterMatch.add(0, subListAfter(argumentResolutions, command.getResolvedArguments()));
        // There may be various commands that would require updated indices, so we only check presence for absence of another invocation
        boolean hasNoPackageManagerInvocation = commandsAfterMatch.stream().allMatch(resolutions -> PACKAGE_MANAGER_DETECTOR.search(resolutions).isEmpty());
        if (hasNoPackageManagerInvocation) {
          ctx.reportIssue(command.textRange(), MESSAGE);
        }
      }
    }
  }

  private static List<ArgumentResolution> subListAfter(List<ArgumentResolution> original, List<ArgumentResolution> part) {
    return original.subList(
      original.indexOf(part.get(part.size() - 1)) + 1,
      original.size());
  }
}
