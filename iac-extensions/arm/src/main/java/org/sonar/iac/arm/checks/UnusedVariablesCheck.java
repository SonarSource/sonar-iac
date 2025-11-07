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
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.checks.utils.CheckUtils;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;

@Rule(key = "S1481")
public class UnusedVariablesCheck extends AbstractUnusedSymbolCheck {

  @Override
  ArmTree.Kind declarationKind() {
    return ArmTree.Kind.VARIABLE_DECLARATION;
  }

  @Override
  protected boolean shouldIgnoreUnused(Declaration declaration) {
    if (declaration instanceof HasDecorators hasDecorators) {
      return hasDecorators.decorators().stream()
        .map(Decorator::expression)
        .anyMatch(CheckUtils.isFunctionCall("export"));
    }
    return false;
  }

  @Override
  String typeOfSymbol() {
    return "variable";
  }
}
