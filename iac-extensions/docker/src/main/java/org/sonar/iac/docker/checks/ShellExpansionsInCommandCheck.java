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

import java.util.Collection;
import java.util.EnumSet;
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
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.sonar.iac.docker.checks.utils.CheckUtils.ignoringSpecificForms;

@Rule(key = "S6573")
public class ShellExpansionsInCommandCheck implements IacCheck {
  private static final String MESSAGE = "Prefix files and paths with \"./\" or \"--\" when using glob.";
  private static final Set<String> EXCEPTION_COMMANDS = Set.of("echo", "printf");
  private static final Set<String> EXCLUDED_COMMANDS = Set.of("find");
  private static final Set<String> EXCEPTION_BASH_TOKENS_BEFORE = Set.of(
    // double-dash signifies the end of program options; wildcards are allowed after it
    "--",
    // pattern matching in for loops is not a subject to the issue
    "for");
  /**
   * We don't run the check on heredoc form or exec form.
   * Here document are not supported with CommandDetector, so we don't run the check on them.
   * Unlike the shell form, the exec form does not invoke a command shell. This means that normal shell processing does not happen.
   */
  private static final Set<DockerTree.Kind> EXCLUDED_FORMS = EnumSet.of(DockerTree.Kind.EXEC_FORM, DockerTree.Kind.HEREDOCUMENT);

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, ignoringSpecificForms(EXCLUDED_FORMS, ShellExpansionsInCommandCheck::check));
    init.register(CmdInstruction.class, ignoringSpecificForms(EXCLUDED_FORMS, ShellExpansionsInCommandCheck::check));
    init.register(EntrypointInstruction.class, ignoringSpecificForms(EXCLUDED_FORMS, ShellExpansionsInCommandCheck::check));
  }

  private static void check(CheckContext ctx, CommandInstruction cmd) {
    SeparatedList<List<ArgumentResolution>, String> splitCommands = ArgumentResolutionSplitter.splitCommands(CheckUtils.resolveInstructionArguments(cmd));
    CommandDetector shellExpansionDetector = CommandDetector.builder()
      .with(ShellExpansionsInCommandCheck::isShellExpansion)
      .build();
    for (List<ArgumentResolution> argumentResolutions : splitCommands.elements()) {
      shellExpansionDetector.search(argumentResolutions).forEach((CommandDetector.Command c) -> {
        List<ArgumentResolution> argumentResolutionsBeforeMatch = argumentResolutions.subList(0, argumentResolutions.indexOf(c.getResolvedArguments().get(0)));
        if (contains(argumentResolutionsBeforeMatch, EXCEPTION_BASH_TOKENS_BEFORE) || isCompliantExcludedCommand(argumentResolutionsBeforeMatch)
          || isCompliantExceptionCommand(argumentResolutionsBeforeMatch)) {
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

  private static boolean isCompliantExcludedCommand(List<ArgumentResolution> argumentResolutionsBeforeWildcard) {
    if (argumentResolutionsBeforeWildcard.isEmpty()) {
      return false;
    }

    // For excluded commands, those are simply not concerned by the rule.
    var command = argumentResolutionsBeforeWildcard.get(0);
    return EXCLUDED_COMMANDS.contains(command.value());
  }

  private static boolean isCompliantExceptionCommand(List<ArgumentResolution> argumentResolutionsBeforeWildcard) {

    // Even exception commands are not allowed to have wildcard as a first argument after flag
    // So we need to check if one of the compliant commands is somewhere before wildcard but not immediately before
    // I.e. `echo *` or `echo -n *` are noncompliant, while `echo 'Files: ' *` is.
    for (int i = argumentResolutionsBeforeWildcard.size() - 1; i >= 0; i--) {
      if (isFlag(argumentResolutionsBeforeWildcard.get(i))) {
        return false;
      } else if (EXCEPTION_COMMANDS.contains(argumentResolutionsBeforeWildcard.get(i).value())) {
        return i != argumentResolutionsBeforeWildcard.size() - 1;
      }
    }
    return false;
  }

  private static boolean isFlag(ArgumentResolution arg) {
    return arg.value().startsWith("-");
  }
}
