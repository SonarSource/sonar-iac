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

import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

abstract class AbstractUnusedSymbolCheck implements IacCheck {
  private static final String MESSAGE = "Remove the unused %s \"%s\".";

  @Override
  public void initialize(InitContext init) {
    init.register(File.class, this::checkSymbols);
  }

  abstract ArmTree.Kind declarationKind();

  abstract String typeOfSymbol();

  void checkSymbols(CheckContext checkContext, File file) {
    var symbolTable = file.symbolTable();
    if (symbolTable != null && !symbolTable.hasFoundUnresolvableSymbolAccess()) {
      symbolTable.getSymbols().forEach(symbol -> {
        var declaration = symbol.findAssignmentDeclaration();
        if (declaration != null && declarationKind() == declaration.getKind() && symbol.isUnused() && !shouldIgnoreUnused(declaration)) {
          reportOnDeclaration(checkContext, declaration);
        }
      });
    }
  }

  private void reportOnDeclaration(CheckContext checkContext, Declaration declaration) {
    checkContext.reportIssue(declaration.declaratedName(), MESSAGE.formatted(typeOfSymbol(), declaration.declaratedName().value()));
  }

  protected boolean shouldIgnoreUnused(Declaration declaration) {
    return false;
  }
}
