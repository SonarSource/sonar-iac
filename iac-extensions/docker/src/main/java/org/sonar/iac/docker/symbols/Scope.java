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
package org.sonar.iac.docker.symbols;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class Scope {

  public enum Kind {
    GLOBAL,
    IMAGE
  }

  private final Map<String, Symbol> symbols = new HashMap<>();

  private final Kind kind;

  public Scope(Kind kind) {
    this.kind = kind;
  }

  public Scope(Kind kind, Scope orgScope) {
    this(kind);
    orgScope.symbols.forEach((name, symbol) -> this.symbols.put(name, new Symbol(symbol)));
  }

  public Symbol addSymbol(String name) {
    return symbols.computeIfAbsent(name, s -> new Symbol(name));
  }

  @Nullable
  public Symbol getSymbol(String name) {
    return symbols.getOrDefault(name, null);
  }

  public List<Symbol> getSymbols() {
    return new ArrayList<>(symbols.values());
  }

  public Kind kind() {
    return kind;
  }
}
