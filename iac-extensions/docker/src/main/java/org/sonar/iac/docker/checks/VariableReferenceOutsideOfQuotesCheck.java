/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.Argument;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.KeyValuePair;
import org.sonar.iac.docker.tree.api.RegularVariable;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6570")
public class VariableReferenceOutsideOfQuotesCheck implements IacCheck {

  private static final String MESSAGE = "Surround this variable with double quotes; otherwise, it can lead to unexpected behavior.";

  @Override
  public void initialize(InitContext init) {
    init.register(RunInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
    init.register(CmdInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
    init.register(EntrypointInstruction.class, VariableReferenceOutsideOfQuotesCheck::checkVariableReference);
  }

  private static void checkVariableReference(CheckContext ctx, CommandInstruction commandInstruction) {
    for (Argument argument : commandInstruction.arguments()) {
      for (Expression expression : argument.expressions()) {
        if (expression.is(DockerTree.Kind.REGULAR_VARIABLE) && !isVariableDefinedWithDoubleQuotes((RegularVariable) expression)) {
          ctx.reportIssue(expression, MESSAGE);
        }
      }
    }
  }

  private static boolean isVariableDefinedWithDoubleQuotes(RegularVariable variable) {
    var symbol = variable.symbol();
    if (symbol != null && symbol.lastDeclaration() != null) {
      var declaration = (KeyValuePair) symbol.lastDeclaration().tree();
      var resolvedValue = ArgumentResolution.ofKeepQuotesForced(declaration.value());
      return resolvedValue.isResolved() && resolvedValue.value().startsWith("\"") && resolvedValue.value().endsWith("\"");
    }
    return false;
  }
}
