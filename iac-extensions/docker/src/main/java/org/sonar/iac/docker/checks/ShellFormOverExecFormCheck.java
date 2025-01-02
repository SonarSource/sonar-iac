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

import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.impl.TextRanges;
import org.sonar.iac.docker.tree.api.CmdInstruction;
import org.sonar.iac.docker.tree.api.CommandInstruction;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.EntrypointInstruction;

@Rule(key = "S7019")
public class ShellFormOverExecFormCheck implements IacCheck {

  private static final String MESSAGE = "Replace this shell form with exec form.";
  private static final Set<Class<? extends CommandInstruction>> classes = Set.of(
    CmdInstruction.class,
    EntrypointInstruction.class);

  @Override
  public void initialize(InitContext init) {
    classes.forEach(klass -> init.register(klass, ShellFormOverExecFormCheck::checkForm));
  }

  private static void checkForm(CheckContext ctx, CommandInstruction commandInstruction) {
    if (commandInstruction.getKindOfArgumentList() == DockerTree.Kind.SHELL_FORM) {
      var firstArg = commandInstruction.arguments().get(0);
      var lastArg = commandInstruction.arguments().get(commandInstruction.arguments().size() - 1);
      var textRange = TextRanges.mergeElementsWithTextRange(List.of(firstArg, lastArg));
      ctx.reportIssue(textRange, MESSAGE);
    }
  }
}
