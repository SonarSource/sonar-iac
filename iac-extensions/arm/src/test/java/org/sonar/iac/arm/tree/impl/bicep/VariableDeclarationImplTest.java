/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.tree.impl.bicep;

import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.typed.ActionParser;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.BooleanLiteral;
import org.sonar.iac.arm.tree.api.NumericLiteral;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.VariableDeclaration;
import org.sonar.iac.arm.tree.api.bicep.HasDecorators;

class VariableDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseVariableDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.VARIABLE_DECLARATION)
      .matches("var foo=42")
      .matches("var foo= 42")
      .matches("var foo =42")
      .matches("var foo = 42")
      .matches("var foo = abc")
      .matches("var foo = true")
      .matches("var foo = 1 > 2")
      .matches("@description('comment') var foo = true")
      .matches("@sys.description('comment') var foo = true");
  }

  @Test
  void shouldParseSimpleVariableDeclaration() {
    VariableDeclaration tree = parse("var foo = 42", BicepLexicalGrammar.VARIABLE_DECLARATION);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(tree.name().value()).isEqualTo("foo");
    softly.assertThat(tree.value()).isInstanceOfAny(NumericLiteral.class);
    softly.assertThat(tree.children()).hasSize(4);
    softly.assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.VARIABLE_DECLARATION);
    softly.assertAll();
  }

  @ParameterizedTest
  @CsvSource({
    "variable foo = 42",
    "@description variable foo = 42",
  })
  void shouldFailOnInvalidVariableDeclaration(String code) {
    Assertions.assertThatThrownBy(() -> parse(code, BicepLexicalGrammar.VARIABLE_DECLARATION)).isInstanceOf(RecognitionException.class);
  }
}
