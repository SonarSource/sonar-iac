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

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.InterpolatedString;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceDeclarationImplTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.RESOURCE_DECLARATION);

  @Test
  void shouldParseMinimalResourceDeclaration() {
    String code = code("resource myName 'type@version' = {",
      "key: value",
      "}\n");

    ResourceDeclaration tree = (ResourceDeclaration) parser.parse(code, null);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.name().value()).isEqualTo("myName");
    assertThat(tree.type().value()).isEqualTo("type");
    assertThat(tree.version().value()).isEqualTo("version");
    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("key");
    assertThat(((StringLiteral) property.value()).value()).isEqualTo("value");

    assertThat(((SyntaxToken) tree.children().get(0)).value()).isEqualTo("resource");
    assertThat(((Identifier) tree.children().get(1)).value()).isEqualTo("myName");
    assertThat(((InterpolatedString) tree.children().get(2)).value()).isEqualTo("type@version");
    assertThat(((SyntaxToken) tree.children().get(3)).value()).isEqualTo("=");
    assertThat(((ObjectExpression) tree.children().get(4)).properties()).hasSize(1);
    assertThat(tree.children()).hasSize(5);
  }

  static Stream<Arguments> shouldParseResourceDeclaration() {
    return Stream.of(
      Arguments.of(
        code("resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {",
          "  name: workloadIpGroupName",
          "  location: location1",
          "}\n"),
        "workloadIpGroup", "Microsoft.Network/ipGroups", "2022-01-01", false,
        List.of("name", "workloadIpGroupName", "location", "location1")),
      Arguments.of(
        code("resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {",
          "  name: workloadIpGroupName",
          "  location: location1",
          "}\n"),
        "workloadIpGroup", "Microsoft.Network/ipGroups", "2022-01-01", false,
        List.of("name", "workloadIpGroupName", "location", "location1")),
      Arguments.of(
        code("resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' = {",
          "  ABC: 123",
          "  myKey: myValue",
          "}\n"),
        "a_b_c", "Microsoft.Network/ipGroups", "2022-01-01", false,
        List.of("ABC", "123", "myKey", "myValue")),
      Arguments.of(
        code("resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {",
          "  ABC: 123",
          "  myKey: myValue",
          "}\n"),
        "a_b_c", "Microsoft.Network/ipGroups", "2022-01-01", true,
        List.of("ABC", "123", "myKey", "myValue")));
  }

  @ParameterizedTest
  @MethodSource
  void shouldParseResourceDeclaration(String code, String name, String type, String version, boolean existing, List<String> keyValuePairs) {
    ResourceDeclaration tree = (ResourceDeclaration) parser.parse(code, null);

    assertThat(tree.name().value()).isEqualTo(name);
    assertThat(tree.type().value()).isEqualTo(type);
    assertThat(tree.version().value()).isEqualTo(version);
    if (existing) {
      assertThat(((SyntaxToken) tree.children().get(3)).value()).isEqualTo("existing");
    }
    if (keyValuePairs.size() % 2 == 1) {
      throw new RuntimeException("There should be even number of key value pairs");
    }
    for (int i = 0; i < keyValuePairs.size(); i = i + 2) {
      Property property = tree.properties().get(i / 2);
      assertThat(property.key().value()).isEqualTo(keyValuePairs.get(i));
      assertThat(((StringLiteral) property.value()).value()).isEqualTo(keyValuePairs.get(i + 1));
    }
  }
}
