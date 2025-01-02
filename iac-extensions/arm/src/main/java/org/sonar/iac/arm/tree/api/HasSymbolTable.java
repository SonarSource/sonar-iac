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
package org.sonar.iac.arm.tree.api;

import javax.annotation.Nullable;
import org.sonar.iac.arm.symbols.SymbolTable;

/**
 * Interface to define any element that has a {@link SymbolTable}, with methods to modify/access it.
 */
public interface HasSymbolTable {
  /**
   * @return the {@link SymbolTable} associated with this element, or null if not set.
   */
  @Nullable
  SymbolTable symbolTable();

  /**
   * @throws IllegalArgumentException when symbolTable is already set
   */
  void setSymbolTable(SymbolTable symbolTable);
}
