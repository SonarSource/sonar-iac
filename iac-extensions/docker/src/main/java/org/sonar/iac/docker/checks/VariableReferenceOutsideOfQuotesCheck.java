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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6570")
public class VariableReferenceOutsideOfQuotesCheck implements IacCheck {

  private static final String MESSAGE = "Add the missing double quotes to this variable, as it can lead to unexpected behaviour.";

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
    init.register(CmdInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
    init.register(EntrypointInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
  }

  private static void checkVariableReference(CheckContext ctx, CommandInstruction commandInstruction) {
    for (Argument argument : commandInstruction.arguments()) {
      for (Expression expression : argument.expressions()) {
        if (expression.is(DockerTree.Kind.REGULAR_VARIABLE)) {
          ctx.reportIssue(expression, MESSAGE);
        }
      }
    }
  }
}
