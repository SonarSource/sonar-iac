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
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class PropertyImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseProperty() {
    Assertions.assertThat(BicepLexicalGrammar.PROPERTY)
      .matches("key:value")
      .matches("'key':value")
      .matches("key: value")
      .matches("key :value")
      .matches("key : value")
      .matches("key1: value1")
      .matches("Ke1: VALu3")

      .notMatches("1key: 1value")
      .notMatches("@abc x value");
  }

  @Test
  void shouldParsePropertyIdentifier() {
    String code = code("key:value");

    Property tree = parse(code, BicepLexicalGrammar.PROPERTY);
    assertThat(((StringLiteral) tree.value()).value()).isEqualTo("value");
    assertThat(tree.is(ArmTree.Kind.PROPERTY)).isTrue();

    Identifier key = (Identifier) tree.children().get(0);
    assertThat(key.value()).isEqualTo("key");

    SyntaxToken colon = (SyntaxToken) tree.children().get(1);
    assertThat(colon.children()).isEmpty();
    assertThat(colon.comments()).isEmpty();

    StringLiteral value = (StringLiteral) tree.children().get(2);
    assertThat(value.value()).isEqualTo("value");

    assertThat(tree.children()).hasSize(3);
  }

  void shouldParsePropertyInterpString() {
    String code = code("'key':value");

    Property tree = parse(code, BicepLexicalGrammar.PROPERTY);
    assertThat(((StringLiteral) tree.value()).value()).isEqualTo("value");
    assertThat(tree.is(ArmTree.Kind.PROPERTY)).isTrue();

    InterpolatedString key = (InterpolatedString) tree.children().get(0);
    assertThat(key.value()).isEqualTo("key");

    SyntaxToken colon = (SyntaxToken) tree.children().get(1);
    assertThat(colon.children()).isEmpty();
    assertThat(colon.comments()).isEmpty();

    StringLiteral value = (StringLiteral) tree.children().get(2);
    assertThat(value.value()).isEqualTo("value");

    assertThat(tree.children()).hasSize(3);
  }
}
