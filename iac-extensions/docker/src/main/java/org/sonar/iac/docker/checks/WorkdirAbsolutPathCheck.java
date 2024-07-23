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

import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.tree.api.WorkdirInstruction;

@Rule(key = "S7021")
public class WorkdirAbsolutPathCheck implements IacCheck {

  private static final Pattern regexAbsolutePath = Pattern.compile("^(?:/|[a-zA-Z]:[/\\\\])");
  private static final String MESSAGE = "Use an absolute path instead of this relative path.";

  @Override
  public void initialize(InitContext init) {
    init.register(WorkdirInstruction.class, WorkdirAbsolutPathCheck::checkWorkdirInstruction);
  }

  private static void checkWorkdirInstruction(CheckContext ctx, WorkdirInstruction workdirInstruction) {
    // We expect only one argument for workdir instruction
    var argument = workdirInstruction.arguments().get(0);
    var argumentResolution = ArgumentResolution.of(argument);
    if (argumentResolution.isResolved() && isRelativePath(argumentResolution.value())) {
      ctx.reportIssue(argument, MESSAGE);
    }
  }

  private static boolean isRelativePath(String path) {
    return !regexAbsolutePath.matcher(path).find();
  }
}
