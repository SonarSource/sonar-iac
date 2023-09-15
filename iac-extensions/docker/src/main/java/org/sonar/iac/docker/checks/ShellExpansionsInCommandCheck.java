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

import java.util.Collection;
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
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6573")
public class ShellExpansionsInCommandCheck implements IacCheck {
  private static final String MESSAGE = "Prefix files and paths with ./ or -- when using glob.";
  private static final Set<String> exceptionCommands = Set.of("echo", "printf");
  private static final Set<String> exceptionBashTokensBefore = Set.of(
    // double-dash signifies the end of program options; wildcards are allowed after it
    "--",
    // pattern matching in for loops is not a subject to the issue
    "for");

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, ShellExpansionsInCommandCheck::check);
    init.register(CmdInstruction.class, ShellExpansionsInCommandCheck::check);
    init.register(EntrypointInstruction.class, ShellExpansionsInCommandCheck::check);
  }

  private static void check(CheckContext ctx, CommandInstruction cmd) {
    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(CheckUtils.resolveInstructionArguments(cmd));
    CommandDetector shellExpansionDetector = CommandDetector.builder()
      .with(ShellExpansionsInCommandCheck::isShellExpansion)
      .build();
    for (List<ArgumentResolution> argumentResolutions : splitCommands.elements()) {
      shellExpansionDetector.search(argumentResolutions).forEach(c -> {
        List<ArgumentResolution> argumentResolutionsBeforeMatch = argumentResolutions.subList(0, argumentResolutions.indexOf(c.getResolvedArguments().get(0)));
        if (contains(argumentResolutionsBeforeMatch, exceptionBashTokensBefore) || isCompliantExceptionCommand(argumentResolutionsBeforeMatch)) {
          return;
        }
        ctx.reportIssue(c.textRange(), MESSAGE);
      });
    }
  }

  private static boolean isShellExpansion(String arg) {
    return arg.startsWith("*") &&
    // Pattern followed by a `)` can belong to Bash case-statement. Moreover, space between `)` and body of the branch is not required.
      !arg.contains(")");
  }

  private static boolean contains(List<ArgumentResolution> argumentResolutions, Collection<String> symbols) {
    for (ArgumentResolution argumentResolution : argumentResolutions) {
      for (String symbol : symbols) {
        if (symbol.equals(argumentResolution.value())) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean isCompliantExceptionCommand(List<ArgumentResolution> argumentResolutionsBeforeWildcard) {
    // Even exception commands are not allowed to have wildcard as a first argument after flag
    // So we need to check if one of the compliant commands is somewhere before wildcard but not immediately before
    // I.e. `echo *` or `echo -n *` are noncompliant, while `echo 'Files: ' *` is.
    if (argumentResolutionsBeforeWildcard.size() < 2 ||
      argumentResolutionsBeforeWildcard.get(argumentResolutionsBeforeWildcard.size() - 1).value().startsWith("-")) {
      return false;
    }
    for (int i = argumentResolutionsBeforeWildcard.size() - 2; i >= 0; i--) {
      if (argumentResolutionsBeforeWildcard.get(i).value().startsWith("-")) {
        return false;
      } else if (exceptionCommands.contains(argumentResolutionsBeforeWildcard.get(i).value())) {
        return true;
      }
    }
    return false;
  }
}
