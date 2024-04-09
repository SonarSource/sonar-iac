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

import org.sonar.iac.arm.tree.api.ArmTree;

public class Usage {

  public enum Kind {
    ASSIGNMENT,
    ACCESS
  }

  private final ArmTree tree;
  private final Kind kind;
  private final SymbolTable symbolTable;

  public Usage(SymbolTable symbolTable, ArmTree tree, Kind kind) {
    this.symbolTable = symbolTable;
    this.tree = tree;
    this.kind = kind;
  }

  public ArmTree tree() {
    return tree;
  }

  public Kind kind() {
    return kind;
  }

  public SymbolTable symbolTable() {
    return symbolTable;
  }
}
