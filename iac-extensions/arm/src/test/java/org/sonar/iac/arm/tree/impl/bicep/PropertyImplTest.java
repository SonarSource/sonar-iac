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

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;
import org.sonar.iac.common.extension.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class PropertyImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.PROPERTY);

  @Test
  void shouldParseSimpleProperty() {
    String code = code("key:value");

    Property tree = (Property) parser.parse(code, null);
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

  static Stream<Arguments> shouldParseValidProperty() {
    return Stream.of(
      Arguments.of("key", ":", "value"),
      Arguments.of("key", ": ", "value"),
      Arguments.of("key", " :", "value"),
      Arguments.of("key", " : ", "value"),
      Arguments.of("key1", ": ", "value1"),
      Arguments.of("Ke1", ": ", "VALu3"));
  }

  @MethodSource
  @ParameterizedTest(name = "should Parse property: `{0}{1}{2}`")
  void shouldParseValidProperty(String key, String colon, String value) {
    String code = code(key + colon + value);

    Property tree = (Property) parser.parse(code, null);
    assertThat(tree.key().value()).isEqualTo(key);
    assertThat(((StringLiteral) tree.value()).value()).isEqualTo(value);
  }

  static Stream<Arguments> shouldNotParseInvalidProperty() {
    return Stream.of(
      Arguments.of("1key", ": ", "1value"),
      Arguments.of("@abc", " x ", "value"));
  }

  @MethodSource
  @ParameterizedTest(name = "should Parse property: `{0}{1}{2}`")
  void shouldNotParseInvalidProperty(String key, String colon, String value) {
    String code = code(key + colon + value);

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("Cannot parse 'null:1:1'");
  }
}
