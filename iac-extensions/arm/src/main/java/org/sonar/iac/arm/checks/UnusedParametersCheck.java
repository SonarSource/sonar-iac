/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.arm.checks;

import org.sonar.check.Rule;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.checks.CheckContext;

@Rule(key = "S6955")
public class UnusedParametersCheck extends AbstractUnusedSymbolCheck {

  @Override
  void checkSymbols(CheckContext checkContext, File file) {
    if (file.targetScopeLiteral() instanceof StringLiteral literal && literal.value().endsWith("deploymentParameters.json#")) {
      // This is to ignore json parameter files
      // We don't need to care about bicep parameter files as they have the extension .bicepparam and are not picked up by the ArmSensor
      return;
    }
    super.checkSymbols(checkContext, file);
  }

  @Override
  ArmTree.Kind declarationKind() {
    return ArmTree.Kind.PARAMETER_DECLARATION;
  }

  @Override
  String typeOfSymbol() {
    return "parameter";
  }
}
