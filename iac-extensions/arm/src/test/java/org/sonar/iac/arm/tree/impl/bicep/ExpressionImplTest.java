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

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.parser.utils.Assertions;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;

class ExpressionImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseExpression() {
    Assertions.assertThat(BicepLexicalGrammar.EXPRESSION)
      .matches("123")
      .matches("true")
      .matches("false")
      .matches("null")
      .matches("abdcef")

      .notMatches(".123456")
      .notMatches("-")
      .notMatches("_abc");
  }

  @Test
  void shouldParseSimpleExpression() {
    StringLiteral tree = parse("123", BicepLexicalGrammar.EXPRESSION);
    assertThat(tree.value()).isEqualTo("123");
    assertThat(tree.is(ArmTree.Kind.STRING_LITERAL)).isTrue();

    assertThat(tree.children()).hasSize(1);
    SyntaxToken token = (SyntaxToken) tree.children().get(0);
    assertThat(token.children()).isEmpty();
    assertThat(token.comments()).isEmpty();
  }
}
