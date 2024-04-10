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
package org.sonar.iac.arm.visitors;

import java.util.EnumSet;
import org.sonar.iac.arm.symbols.SymbolTable;
import org.sonar.iac.arm.symbols.Usage;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.common.extension.visitors.InputFileContext;
import org.sonar.iac.common.extension.visitors.TreeVisitor;

/**
 * Class used to visit a ArmTree and build symbols and their usages for variables.
 * Those Symbol/Usage can later be used in checks to resolve Argument(really?) or to report issue variable flow.
 */

public class ArmSymbolVisitor extends TreeVisitor<InputFileContext> {
  private static final EnumSet<ArmTree.Kind> PARENT_KIND_INDICATING_NO_USAGE = EnumSet.of(
    ArmTree.Kind.VARIABLE_DECLARATION,
    ArmTree.Kind.OUTPUT_DECLARATION,
    ArmTree.Kind.PROPERTY);
  private SymbolTable currentSymbolTable = new SymbolTable();

  public ArmSymbolVisitor() {
    register(File.class, (ctx, file) -> visitFile(file));
    register(VariableDeclaration.class, (ctx, variableDeclaration) -> visitVariableDeclaration(variableDeclaration));
    register(Identifier.class, (ctx, identifier) -> visitIdentifier(identifier));
  }

  void visitFile(File file) {
    currentSymbolTable = new SymbolTable();
    file.setSymbolTable(currentSymbolTable);
  }

  private void visitVariableDeclaration(VariableDeclaration variableDeclaration) {
    var symbol = currentSymbolTable.addSymbol(variableDeclaration.declaratedName().value());
    symbol.addUsage(currentSymbolTable, variableDeclaration, Usage.Kind.ASSIGNMENT);
  }

  void visitIdentifier(Identifier identifier) {
    if (!shouldRegisterAsUsage(identifier)) {
      return;
    }

    if (identifier.symbol() != null) {
      return;
    }

    var symbol = currentSymbolTable.getSymbol(identifier.value());
    if (symbol != null) {
      symbol.addUsage(currentSymbolTable, identifier, Usage.Kind.ACCESS);
    }
  }

  private static boolean shouldRegisterAsUsage(Identifier identifier) {
    var identifierParent = identifier.parent();
    if (identifierParent == null) {
      // Identifier without parent are invalid /malformed, so they should not register as usage
      return false;
    }

    try {
      identifierParent.getKind();
    } catch (UnsupportedOperationException e) {
      // Workaround for avoiding failing on Trees without specified Kinds
      return true;
    }

    return !PARENT_KIND_INDICATING_NO_USAGE.contains(identifierParent.getKind());
  }
}
