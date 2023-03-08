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
import org.sonar.api.batch.fs.TextRange;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.utils.ArgumentUtils;

import static org.sonar.iac.docker.utils.ArgumentUtils.ArgumentResolution.Status.UNRESOLVED;

@Rule(key = "S6500")
public class PackageInstallationCheck implements IacCheck {

  private static final String MESSAGE = "Make sure that installing unnecessary dependencies is safe here.";

  private static final Map<String, String> COMMAND_FLAG_MAP = Map.of(
    "apt", "--no-install-recommends",
    "apt-get", "--no-install-recommends",
    "aptitude", "--without-recommends"
  );

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, PackageInstallationCheck::checkRunInstruction);
  }

  private static void checkRunInstruction(CheckContext ctx, RunInstruction instruction) {
    StatementValidator validator = new StatementValidator(ctx);
    instruction.arguments().forEach(validator::process);
  }

  private static class StatementValidator {

    CheckContext ctx;
    AptStatement statement;

    StatementValidator(CheckContext ctx) {
      this.ctx = ctx;
    }

    void process(Argument argument) {
      ArgumentUtils.ArgumentResolution resolution = ArgumentUtils.resolve(argument);

      // stop analyzing apt statement when unresolved part is detected
      if (resolution.is(UNRESOLVED)) {
        statement = null;
        return;
      }
      String argValue = resolution.value();

      // if new apt statement starts report existing one and create new statement
      if (COMMAND_FLAG_MAP.containsKey(argValue)) {
        reportSensitiveStatement();
        statement = new AptStatement(argument, COMMAND_FLAG_MAP.get(argValue));
      } else if (statement != null) {
        // check for required flag or store irrelevant ones to the statement
        if (argValue.startsWith("-")) {
          if (statement.requiredFlag.equals(argValue)) {
            statement = null;
          } else {
            statement.add(argument);
          }
        // detect if statement is installed command
        } else if ("install".equals(argValue)) {
          statement.isInstallStatement = true;
          statement.add(argument);
        // report statement if install statement without required flag
        } else {
          reportSensitiveStatement();
        }
      }
    }

    private void reportSensitiveStatement() {
      if (statement != null && statement.isInstallStatement) {
        ctx.reportIssue(mergeArgumentTextRange(statement.arguments), MESSAGE);
        statement = null;
      }
    }

    private static TextRange mergeArgumentTextRange(List<Argument> arguments) {
      List<TextRange> textRanges = new ArrayList<>();
      for (Argument argument : arguments) {
        textRanges.add(argument.textRange());
      }
      return TextRanges.merge(textRanges);
    }

    private static class AptStatement {
      List<Argument> arguments = new ArrayList<>();
      boolean isInstallStatement = false;
      String requiredFlag;

      public AptStatement(Argument command, String requiredFlag) {
        arguments.add(command);
        this.requiredFlag = requiredFlag;
      }

      public void add(Argument element) {
        arguments.add(element);
      }
    }
  }
}
