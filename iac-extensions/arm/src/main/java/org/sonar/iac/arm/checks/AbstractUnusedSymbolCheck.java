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
package org.sonar.iac.arm.checks;

import javax.annotation.CheckForNull;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.bicep.Declaration;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;

abstract class AbstractUnusedSymbolCheck implements IacCheck {
  private static final String MESSAGE = "Remove the unused %s \"%s\".";

  @Override
  public void initialize(InitContext init) {
    init.register(File.class, this::checkSymbol);
  }

  abstract Class<? extends Declaration> declarationClass();

  abstract String typeOfSymbol();

  void checkSymbol(CheckContext checkContext, File file) {
    var symbolTable = file.symbolTable();
    if (symbolTable != null && !symbolTable.hasFoundUnresolvableSymbolAccess()) {
      symbolTable.getSymbols().forEach(symbol -> {
        var declaration = retrieveDeclaration(symbol);
        if (declarationClass().isInstance(declaration) && isUnused(symbol)) {
          reportOnDeclaration(checkContext, declaration);
        }
      });
    }
  }

  private static boolean isUnused(Symbol symbol) {
    return symbol.usages().stream().noneMatch(usage -> Usage.Kind.ACCESS == usage.kind());
  }

  @CheckForNull
  private static Declaration retrieveDeclaration(Symbol symbol) {
    return symbol.usages().stream()
      .filter(usage -> Usage.Kind.ASSIGNMENT == usage.kind())
      .map(Usage::tree)
      .filter(Declaration.class::isInstance)
      .map(Declaration.class::cast)
      .findFirst()
      .orElse(null);
  }

  private void reportOnDeclaration(CheckContext checkContext, Declaration declaration) {
    checkContext.reportIssue(declaration.declaratedName(), MESSAGE.formatted(typeOfSymbol(), declaration.declaratedName().value()));
  }
}
