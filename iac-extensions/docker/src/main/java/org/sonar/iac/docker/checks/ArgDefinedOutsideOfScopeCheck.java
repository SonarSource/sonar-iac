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
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.tree.api.ArgInstruction;
import org.sonar.iac.docker.tree.api.DockerImage;
import org.sonar.iac.docker.tree.api.Expression;
import org.sonar.iac.docker.tree.api.Literal;
import org.sonar.iac.docker.tree.api.RunInstruction;
import org.sonar.iac.docker.tree.api.Variable;

@Rule(key = "S6579")
public class ArgDefinedOutsideOfScopeCheck implements IacCheck {
  private static final String MESSAGE = "Include the ARG instruction in the build stage where it is used.";

  @Override
  public void initialize(InitContext init) {
    init.register(DockerImage.class, ArgDefinedOutsideOfScopeCheck::checkReferencedVariablesAreInScope);
  }

  private static void checkReferencedVariablesAreInScope(CheckContext ctx, DockerImage image) {
    List<String> argNamesInScope = image.instructions().stream()
      .filter(ArgInstruction.class::isInstance)
      .map(ArgInstruction.class::cast)
      .flatMap(it -> it.keyValuePairs().stream())
      .map(pair -> {
        Expression e = pair.key().expressions().get(0);
        return ((Literal) e).value();
      })
      .collect(Collectors.toList());

    List<Variable> usedVariables = image.instructions().stream()
      .filter(RunInstruction.class::isInstance)
      .flatMap(run -> ((RunInstruction) run).arguments().stream())
      .flatMap(arg -> arg.expressions().stream())
      .filter(Variable.class::isInstance)
      .map(Variable.class::cast)
      .collect(Collectors.toList());

    usedVariables.forEach(variable -> {
      if (!argNamesInScope.contains(variable.identifier())) {
        ctx.reportIssue(variable.textRange(), MESSAGE);
      }
    });
  }
}
