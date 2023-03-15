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
import java.util.Map;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRange;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;

import static org.sonar.iac.docker.symbols.ArgumentResolution.Status.UNRESOLVED;

@Rule(key = "S6505")
public class PackageInstallationScriptExecutionCheck implements IacCheck {

  private static final String MESSAGE = "Omitting --ignore-scripts can lead to the execution of shell scripts. Make sure it is safe here.";

  private static final String REQUIRED_FLAG = "--ignore-scripts";

  private enum StatementType {
    NPM,
    YARN
  }

  private static final Map<String, StatementType> TRIGGER_COMMANDS_STATEMENT_TYPE_MAPPING = Map.of(
    "npm", StatementType.NPM,
    "pnpm", StatementType.NPM,
    "yarn", StatementType.YARN);

  private static final Set<String> NPM_INSTALL_ALIASES = Set.of("ci", "add", "i", "in", "ins", "inst", "insta", "instal", "isnt", "isnta", "isntal", "isntall");

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageInstallationScriptExecutionCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction instruction) {
    StatementValidator validator = new StatementValidator(ctx, instruction.arguments());
    validator.processArguments();
  }

  private static class StatementValidator {

    CheckContext ctx;
    List<Argument> arguments;
    PackageManagerStatement currentStatement;

    StatementValidator(CheckContext ctx, List<Argument> arguments) {
      this.ctx = ctx;
      this.arguments = arguments;
    }

    void processArguments() {
      if (arguments == null || arguments.isEmpty()) {
        return;
      }
      arguments.forEach(this::process);
      reportSensitiveStatement();
    }

    void process(Argument argument) {
      ArgumentResolution resolution = ArgumentResolution.of(argument);

      // stop analyzing package-manager statement when unresolved part is detected
      if (resolution.is(UNRESOLVED)) {
        currentStatement = null;
        return;
      }
      String argValue = resolution.value();

      // if new package-manager statement starts report existing one and create new statement
      if (TRIGGER_COMMANDS_STATEMENT_TYPE_MAPPING.containsKey(argValue)) {
        reportSensitiveStatement();
        currentStatement = new PackageManagerStatement(argument, REQUIRED_FLAG, TRIGGER_COMMANDS_STATEMENT_TYPE_MAPPING.get(argValue));
      } else if (currentStatement != null) {
        // check for required flag or store irrelevant ones to the statement
        if (argValue.startsWith("-")) {
          if (currentStatement.requiredFlag.equals(argValue)) {
            currentStatement = null;
          } else {
            currentStatement.add(argument);
          }
          // detect if statement is installed command or known alias
        } else if ("install".equals(argValue) || (StatementType.NPM == currentStatement.statementType && NPM_INSTALL_ALIASES.contains(argValue))) {
          currentStatement.isInstallStatement = true;
          currentStatement.add(argument);
          // report statement if install statement without required flag
        } else {
          reportSensitiveStatement();
        }
      }

    }

    private void reportSensitiveStatement() {
      if (currentStatement != null && (currentStatement.isInstallStatement ||
        (StatementType.YARN == currentStatement.statementType && arguments.size() == 1))) {
        // also reports the statement if the statement consists only of the "yarn" command
        ctx.reportIssue(mergeArgumentTextRange(currentStatement.arguments), MESSAGE);
        currentStatement = null;
      }
    }

    private static TextRange mergeArgumentTextRange(List<Argument> arguments) {
      List<TextRange> textRanges = new ArrayList<>();
      for (Argument argument : arguments) {
        textRanges.add(argument.textRange());
      }
      return TextRanges.merge(textRanges);
    }

    private static class PackageManagerStatement {

      List<Argument> arguments = new ArrayList<>();
      StatementType statementType;
      boolean isInstallStatement = false;
      String requiredFlag;

      public PackageManagerStatement(Argument command, String requiredFlag, StatementType statementType) {
        arguments.add(command);
        this.statementType = statementType;
        this.requiredFlag = requiredFlag;
      }

      public void add(Argument element) {
        arguments.add(element);
      }
    }
  }
}
