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
package org.sonar.iac.arm.symbols;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.api.bicep.Declaration;

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

  public boolean isUnused() {
    return usages.stream().noneMatch(usage -> Usage.Kind.ACCESS == usage.kind());
  }

  @CheckForNull
  public Declaration findAssignmentDeclaration() {
    return usages.stream()
      .filter(usage -> Usage.Kind.ASSIGNMENT == usage.kind())
      .map(Usage::tree)
      .filter(Declaration.class::isInstance)
      .map(Declaration.class::cast)
      .findFirst()
      .orElse(null);
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
