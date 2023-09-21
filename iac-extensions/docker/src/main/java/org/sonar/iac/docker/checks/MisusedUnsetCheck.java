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

import java.util.List;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.checks.utils.CheckUtils;
import org.sonar.iac.docker.checks.utils.CommandDetector;
import org.sonar.iac.docker.symbols.ArgumentResolution;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RunInstruction;

@Rule(key = "S6581")
public class MisusedUnsetCheck implements IacCheck {
  private static final String MESSAGE = "Use the ARG instruction or set and unset the environment variable in a single layer.";

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, (ctx, image) -> image.instructions().stream()
      .filter(RunInstruction.class::isInstance)
      .map(RunInstruction.class::cast)
      .forEach(runInstruction -> checkCommandsIn(runInstruction, ctx, image.scope())));
  }

  private static void checkCommandsIn(RunInstruction runInstruction, CheckContext ctx, Scope scope) {
    List<ArgumentResolution> argumentResolutions = CheckUtils.resolveInstructionArguments(runInstruction);
    CommandDetector unsetDetector = CommandDetector.builder()
      .with("unset")
      // -f unsets function names in Bash, while we only care about variables
      .withAnyFlagExcept("-f")
      .withOptionalRepeating(s -> true)
      .build();
    unsetDetector.search(argumentResolutions).forEach(c -> checkMatchedCommand(c, ctx, scope));
  }

  private static void checkMatchedCommand(CommandDetector.Command command, CheckContext ctx, Scope scope) {
    command.getResolvedArguments().stream()
      // first element of matched command is `unset` call...
      .skip(1)
      // ... and then possibly some flags
      .dropWhile(resolution -> resolution.value().startsWith("-"))
      .forEach(resolution -> reportArgumentIfNonCompliant(resolution, ctx, scope));
  }

  private static void reportArgumentIfNonCompliant(ArgumentResolution resolution, CheckContext ctx, Scope scope) {
    // Arguments of RUN instruction should always be string literals
    Literal argument = (Literal) resolution.argument().expressions().get(0);
    var symbol = scope.getSymbol(argument.value());
    if (symbol != null && isDefinedAsEnv(symbol)) {
      ctx.reportIssue(argument.textRange(), MESSAGE);
    }
  }

  private static boolean isDefinedAsEnv(Symbol symbol) {
    // `.tree()` is `KeyValuePair`; `parent()` gives the actual node
    return symbol.usages().get(0).tree().parent().is(DockerTree.Kind.ENV);
  }
}
