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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Parameter;
import org.sonar.iac.arm.tree.api.Variable;

public class SymbolTable {
  private final Map<String, Symbol> symbols = new HashMap<>();

  // Currently we can't differentiate between unresolved variable and parameter references in bicep, because every access counts as variable
  // In bicep there can be no unresolvedReferences...
  // Fixme!
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
