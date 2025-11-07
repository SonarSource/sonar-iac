/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;

@Rule(key = "S7021")
public class WorkdirAbsolutPathCheck implements IacCheck {

  private static final Pattern regexAbsolutePath = Pattern.compile("^(?:/|[a-zA-Z]:[/\\\\]|~|%[a-zA-Z()86]++%)");
  private static final String MESSAGE = "Use an absolute path instead of this relative path when defining the WORKDIR.";

  @Override
  public void initialize(InitContext init) {
    init.register(WorkdirInstruction.class, WorkdirAbsolutPathCheck::checkWorkdirInstruction);
  }

  private static void checkWorkdirInstruction(CheckContext ctx, WorkdirInstruction workdirInstruction) {
    // We expect only one argument for workdir instruction
    var argument = workdirInstruction.arguments().get(0);
    var argumentResolution = ArgumentResolution.of(argument);
    if (argumentResolution.isResolved() && !argumentResolution.value().isEmpty() && isRelativePath(argumentResolution.value())) {
      ctx.reportIssue(argument, MESSAGE);
    }
  }

  private static boolean isRelativePath(String path) {
    return !regexAbsolutePath.matcher(path).find();
  }
}
