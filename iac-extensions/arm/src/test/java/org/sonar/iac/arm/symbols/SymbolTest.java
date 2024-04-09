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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.HasSymbol;
import org.sonar.iac.arm.tree.impl.json.IdentifierImpl;
import org.sonar.iac.arm.tree.impl.json.StringLiteralImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SymbolTest {

  @Test
  void usageInIdentifierShouldBehaveAsExpected() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    IdentifierImpl fooIdentifier = new IdentifierImpl("foo", null);
    symbol.addUsage(symbolTable, fooIdentifier, Usage.Kind.ACCESS);

    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.usages()).hasSize(1);
    assertThat(symbol.usages().get(0).kind()).isEqualTo(Usage.Kind.ACCESS);
    assertThat(symbol.usages().get(0).tree()).isEqualTo(fooIdentifier);

    assertThat(fooIdentifier.symbol()).isEqualTo(symbol);
  }

  @Test
  void shouldThrowExceptionWhenTryingToAddSecondSymbol() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    IdentifierImpl fooIdentifier = new IdentifierImpl("foo", null);
    symbol.addUsage(symbolTable, fooIdentifier, Usage.Kind.ACCESS);

    assertThatExceptionOfType(IllegalArgumentException.class)
      .isThrownBy(() -> symbol.addUsage(symbolTable, fooIdentifier, Usage.Kind.ACCESS))
      .withMessage("A symbol is already set");
  }

  @Test
  void usageInTreeWithoutSymbolShouldNotFail() {
    SymbolTable symbolTable = new SymbolTable();
    Symbol symbol = symbolTable.addSymbol("foo");

    StringLiteralImpl fooLiteral = new StringLiteralImpl("foo", null);
    symbol.addUsage(symbolTable, fooLiteral, Usage.Kind.ACCESS);

    assertThat(fooLiteral).isNotInstanceOf(HasSymbol.class);
    assertThat(symbol.name()).isEqualTo("foo");
    assertThat(symbol.usages()).hasSize(1);
    assertThat(symbol.usages().get(0).kind()).isEqualTo(Usage.Kind.ACCESS);
    assertThat(symbol.usages().get(0).tree()).isEqualTo(fooLiteral);
  }
}
