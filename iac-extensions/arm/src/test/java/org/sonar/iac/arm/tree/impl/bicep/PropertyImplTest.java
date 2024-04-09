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
package org.sonar.iac.arm.tree.impl.bicep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.HasIdentifier;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class PropertyImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseProperty() {
    ArmAssertions.assertThat(BicepLexicalGrammar.PROPERTY)
      .matches("key:value")
      .matches("'key':value")
      .matches("'a${123}b${456}c':value")
      .matches("'a${123}${456}c':value")
      .matches("key: value")
      .matches("key: 1 > 2")
      .matches("key :value")
      .matches("key : value")
      .matches("key1: value1")
      .matches("Ke1: VALu3")
      .matches("key: 'string'")
      // defining a key of name the same as keyword is possible
      .matches("if: 'string'")
      .matches("type: 'string'")
      .matches("for: 'string'")
      .matches("metadata: 'string'")
      .matches("func: 'string'")

      .notMatches("1key: 1value")
      .notMatches("@abc x value");
  }

  @Test
  void shouldParsePropertyIdentifier() {
    String code = code("key:value");

    Property tree = parse(code, BicepLexicalGrammar.PROPERTY);
    assertThat(tree.value()).asWrappedIdentifier().hasValue("value");
    assertThat(tree.is(ArmTree.Kind.PROPERTY)).isTrue();

    Assertions.assertThat(((ArmTree) tree.children().get(0)).getKind()).isEqualTo(ArmTree.Kind.IDENTIFIER);
    Identifier key = (Identifier) tree.children().get(0);
    assertThat(key.value()).isEqualTo("key");

    SyntaxToken colon = (SyntaxToken) tree.children().get(1);
    assertThat(colon.children()).isEmpty();
    assertThat(colon.comments()).isEmpty();

    Assertions.assertThat(((ArmTree) tree.children().get(2)))
      .satisfies(t -> ArmAssertions.assertThat(t).is(ArmTree.Kind.VARIABLE))
      .extracting(e -> ((HasIdentifier) e).identifier())
      .extracting(e -> ((Identifier) e).value())
      .isEqualTo("value");

    assertThat(tree.children()).hasSize(3);
  }

  @Test
  void shouldParsePropertyInterpString() {
    String code = code("'key':value");

    Property tree = parse(code, BicepLexicalGrammar.PROPERTY);
    assertThat(tree.value()).asWrappedIdentifier().hasValue("value");
    assertThat(tree.is(ArmTree.Kind.PROPERTY)).isTrue();

    Assertions.assertThat(((ArmTree) tree.children().get(0)).getKind()).isEqualTo(ArmTree.Kind.STRING_LITERAL);
    InterpolatedString key = (InterpolatedString) tree.children().get(0);

    SyntaxToken colon = (SyntaxToken) tree.children().get(1);
    assertThat(colon.children()).isEmpty();
    assertThat(colon.comments()).isEmpty();

    Assertions.assertThat(((ArmTree) tree.children().get(2)))
      .satisfies(t -> ArmAssertions.assertThat(t).is(ArmTree.Kind.VARIABLE))
      .extracting(e -> ((HasIdentifier) e).identifier())
      .extracting(e -> ((Identifier) e).value())
      .isEqualTo("value");

    assertThat(tree.children()).hasSize(3);
  }
}
