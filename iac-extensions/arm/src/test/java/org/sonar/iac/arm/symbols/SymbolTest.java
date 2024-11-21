/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.impl.VariableImpl;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SymbolTest {

  @Test
  void usageInIdentifierShouldBehaveAsExpected() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    VariableImpl fooVariable = new VariableImpl(new IdentifierImpl("foo", null), null);
    symbol.addUsage(fooVariable, Usage.Kind.ACCESS);

    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.usages()).hasSize(1);
    assertThat(symbol.usages().get(0).kind()).isEqualTo(Usage.Kind.ACCESS);
    assertThat(symbol.usages().get(0).tree()).isEqualTo(fooVariable);

    assertThat(fooVariable.symbol()).isEqualTo(symbol);
  }

  @Test
  void shouldThrowExceptionWhenTryingToAddSecondSymbol() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    VariableImpl fooVariable = new VariableImpl(new IdentifierImpl("foo", null), null);
    symbol.addUsage(fooVariable, Usage.Kind.ACCESS);

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> symbol.addUsage(fooVariable, Usage.Kind.ACCESS))
      .withMessage("A symbol is already set");
  }

  @Test
  void usageInTreeWithoutSymbolShouldNotFail() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    StringLiteralImpl fooLiteral = new StringLiteralImpl("foo", null);
    symbol.addUsage(fooLiteral, Usage.Kind.ACCESS);

    assertThat(fooLiteral).isNotInstanceOf(HasSymbol.class);
    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.usages()).hasSize(1);
    assertThat(symbol.usages().get(0).kind()).isEqualTo(Usage.Kind.ACCESS);
    assertThat(symbol.usages().get(0).tree()).isEqualTo(fooLiteral);
  }
}
