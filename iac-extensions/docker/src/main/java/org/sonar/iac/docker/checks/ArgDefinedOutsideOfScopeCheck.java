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

import java.util.Objects;
import java.util.stream.Stream;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.docker.symbols.Scope;
import org.sonar.iac.docker.tree.api.DockerTree;
import org.sonar.iac.docker.tree.api.Variable;

import static org.sonar.iac.docker.tree.TreeUtils.firstAncestorOfKind;

@Rule(key = "S6579")
public class ArgDefinedOutsideOfScopeCheck implements IacCheck {
  private static final String MESSAGE = "Include the ARG instruction in the build stage where it is used.";

  @Override
  public void initialize(InitContext init) {
    init.register(Variable.class, ArgDefinedOutsideOfScopeCheck::checkVariableIsDefinedLocally);
  }

  private static void checkVariableIsDefinedLocally(CheckContext ctx, Variable variable) {
    var symbol = variable.symbol();
    if (symbol == null || Stream.iterate(variable, Objects::nonNull, DockerTree::parent).anyMatch(t -> t.is(DockerTree.Kind.FROM))) {
      // Variable is either not a Dockerfile variable or is part of a FROM instruction where it doesn't need to be redeclared
      return;
    }

    var currentUsageScope = firstAncestorOfKind(variable, DockerTree.Kind.DOCKERIMAGE, DockerTree.Kind.BODY)
      .map(DockerTree::getKind)
      .orElse(null);
    if (Scope.Kind.GLOBAL == symbol.lastDeclarationScope() && currentUsageScope == DockerTree.Kind.DOCKERIMAGE) {
      ctx.reportIssue(variable.textRange(), MESSAGE);
    }
  }
}
