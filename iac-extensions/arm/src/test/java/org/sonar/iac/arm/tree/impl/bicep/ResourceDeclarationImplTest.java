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
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Identifier;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.StringComplete;
import org.sonar.iac.arm.tree.api.bicep.SyntaxToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceDeclarationImplTest extends BicepTreeModelTest {

  BicepParser parser = BicepParser.create(BicepLexicalGrammar.RESOURCE_DECLARATION);

  @Test
  void shouldParseMinimalResourceDeclaration() {
    String code = code("resource myName 'type@version' = {",
      "key: value",
      "}");

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
    assertThat(((StringComplete) tree.children().get(2)).value()).isEqualTo("type@version");
    assertThat(((SyntaxToken) tree.children().get(3)).value()).isEqualTo("=");
    assertThat(((ObjectExpression) tree.children().get(4)).properties()).hasSize(1);
    assertThat(((SyntaxToken) tree.children().get(5)).value()).isBlank();
    assertThat(tree.children()).hasSize(6);
  }

  @Test
  void shouldParseResourceDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.RESOURCE_DECLARATION)
      .matches(code("resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {",
        "  name: workloadIpGroupName",
        "  location: location1",
        "}"))
      .matches(code("resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {",
        "  name: workloadIpGroupName",
        "  location: location1",
        "}"))
      .matches(code("resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' = {",
        "  ABC: 123",
        "  myKey: myValue",
        "}"))
      .matches(code("resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {",
        "  ABC: 123",
        "  myKey: myValue",
        "}"))

      .notMatches(code("resource myName 'type_version' = {",
        "abc",
        "}"));
  }

  @Test
  void shouldThrowExceptionForInvalidTypeAndVersion() {
    String code = code("resource myName 'type_version' = {",
      "}");

    ResourceDeclaration tree = (ResourceDeclaration) parser.parse(code, null);
    assertThatThrownBy(() -> tree.type())
      .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> tree.version())
      .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void shouldThrowExceptionForInvalidTypeAndVersion2() {
    String code = code("resource myName 'foo@bar@baz' = {",
      "}");

    ResourceDeclaration tree = (ResourceDeclaration) parser.parse(code, null);
    assertThatThrownBy(() -> tree.type())
      .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> tree.version())
      .isInstanceOf(UnsupportedOperationException.class);
  }
}
