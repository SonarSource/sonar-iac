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
package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import javax.annotation.Nullable;
import org.sonar.iac.arm.symbols.Symbol;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.impl.AbstractArmTreeImpl;
import org.sonar.iac.common.api.tree.Tree;

public class VariableDeclarationImpl extends AbstractArmTreeImpl implements VariableDeclaration {

  private final Identifier name;
  private final Expression value;
  private Symbol symbol;

  public VariableDeclarationImpl(Identifier name, Expression value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Identifier declaratedName() {
    return name;
  }

  @Override
  public Expression value() {
    return value;
  }

  @Override
  public List<Tree> children() {
    return List.of(name, value);
  }

  @Override
  public Kind getKind() {
    return Kind.VARIABLE_DECLARATION;
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
}
