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

import java.util.Objects;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.symbols.Usage;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Variable;

@Rule(key = "S6579")
public class ArgDefinedOutsideOfScopeCheck implements IacCheck {
  private static final String MESSAGE = "Include the ARG instruction in the build stage where it is used.";

  @Override
  public void initialize(InitContext init) {
    init.register(Variable.class, ArgDefinedOutsideOfScopeCheck::checkVariableIsDefinedLocally);
  }

  private static void checkVariableIsDefinedLocally(CheckContext ctx, Variable variable) {
    if (variable.symbol() == null || Stream.iterate(variable, Objects::nonNull, DockerTree::parent).anyMatch(t -> t.is(DockerTree.Kind.FROM))) {
      // Variable is either not a Dockerfile variable or is path of FROM instruction where it doesn't need to be redeclared
      return;
    }

    boolean isDefinedInImageScope = false;
    boolean isDefinedInGlobalScope = false;
    for (Usage usage : variable.symbol().usages()) {
      if (usage.kind().equals(Usage.Kind.ASSIGNMENT)) {
        isDefinedInImageScope = usage.scope().kind().equals(Scope.Kind.IMAGE);
        isDefinedInGlobalScope = usage.scope().kind().equals(Scope.Kind.GLOBAL);
      }
    }
    boolean isDefinedOnlyInGlobalScope = isDefinedInGlobalScope && !isDefinedInImageScope;
    if (isDefinedOnlyInGlobalScope) {
      ctx.reportIssue(variable.textRange(), MESSAGE);
    }
  }
}
