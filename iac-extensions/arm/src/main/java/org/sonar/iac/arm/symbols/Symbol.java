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
package org.sonar.iac.arm.symbols;

import java.util.ArrayList;
import java.util.List;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasSymbol;

public class Symbol {
  private final SymbolTable symbolTable;
  private final String name;
  private final List<Usage> usages;

  public Symbol(SymbolTable symbolTable, String name) {
    this.symbolTable = symbolTable;
    this.name = name;
    this.usages = new ArrayList<>();
  }

  public void addUsage(ArmTree tree, Usage.Kind kind) {
    var usage = new Usage(tree, kind);
    usages.add(usage);
    if (tree instanceof HasSymbol treeWithSymbol) {
      treeWithSymbol.setSymbol(this);
    }
  }

  public SymbolTable symbolTable() {
    return symbolTable;
  }

  public String name() {
    return name;
  }

  public List<Usage> usages() {
    return usages;
  }
}
