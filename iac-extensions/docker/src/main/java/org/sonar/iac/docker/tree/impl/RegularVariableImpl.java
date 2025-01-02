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
package org.sonar.iac.docker.tree.impl;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.docker.symbols.Symbol;
import org.sonar.iac.docker.tree.api.RegularVariable;
import org.sonar.iac.docker.tree.api.SyntaxToken;

public class RegularVariableImpl extends AbstractDockerTreeImpl implements RegularVariable {

  private final SyntaxToken dollar;
  private final SyntaxToken identifier;
  private Symbol symbol;

  public RegularVariableImpl(SyntaxToken dollar, SyntaxToken identifier) {
    this.dollar = dollar;
    this.identifier = identifier;
  }

  @Override
  public String identifier() {
    return identifier.value();
  }

  @Override
  public List<Tree> children() {
    return List.of(dollar, identifier);
  }

  @Override
  public Kind getKind() {
    return Kind.REGULAR_VARIABLE;
  }

  @Nullable
  @Override
  public Symbol symbol() {
    return symbol;
  }

  @Override
  public void setSymbol(Symbol symbol) {
    if (this.symbol != null) {
      throw new IllegalArgumentException("A symbol is already set");
    }
    this.symbol = symbol;
  }

  @Override
  public String toString() {
    return dollar + identifier.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RegularVariableImpl that)) {
      return false;
    }
    return Objects.equals(dollar, that.dollar) && Objects.equals(identifier, that.identifier) && Objects.equals(symbol, that.symbol);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dollar, identifier, symbol);
  }
}
