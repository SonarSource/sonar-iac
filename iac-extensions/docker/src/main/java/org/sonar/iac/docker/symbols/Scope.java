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
