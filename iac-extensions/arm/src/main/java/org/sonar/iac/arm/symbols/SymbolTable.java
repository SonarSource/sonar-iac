/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
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
package org.sonar.iac.arm.symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.HasIdentifier;

public class SymbolTable {
  private final Map<String, Symbol> symbols = new HashMap<>();
  private final Set<HasIdentifier> unresolvedReferences = new HashSet<>();

  public Symbol addSymbol(String name) {
    return symbols.computeIfAbsent(name, s -> new Symbol(this, name));
  }

  @Nullable
  public Symbol getSymbol(String name) {
    return symbols.getOrDefault(name, null);
  }

  public List<Symbol> getSymbols() {
    return new ArrayList<>(symbols.values());
  }

  public void foundUnresolvableSymbolAccess(HasIdentifier hasIdentifier) {
    unresolvedReferences.add(hasIdentifier);
  }

  public Set<HasIdentifier> getUnresolvedReferences() {
    return unresolvedReferences;
  }

  public boolean hasFoundUnresolvableSymbolAccess() {
    return !unresolvedReferences.isEmpty();
  }
}
