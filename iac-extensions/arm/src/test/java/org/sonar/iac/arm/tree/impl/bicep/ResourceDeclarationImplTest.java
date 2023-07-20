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

import org.fest.assertions.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.bicep.Decorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseResourceDeclarationObject() {
    String code = code("resource myName 'type@version' = { key: value }");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.name().value()).isEqualTo("myName");
    assertThat(tree.type().value()).isEqualTo("type");
    assertThat(tree.version().value()).isEqualTo("version");

    assertThat(tree.properties()).isEmpty();
    assertThat(tree.existing()).isFalse();

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("resource", "myName", "type@version", "=", "{", "key", ":", "value", "}");
  }

  @Test
  void shouldParseResourceDeclarationObjectAndReadProperties() {
    String code = code("resource myName 'type@version' = { key: value",
      "properties: {",
      "prop1: val1",
      "}",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
  }

  @Test
  void shouldRetrievePropertiesFromIfCondition() {
    String code = code("resource myName 'type@version' = if (!empty(logAnalytics)) {",
      "key: value",
      "properties: {",
      "prop1: val1",
      "}",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.existing()).isFalse();

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("resource", "myName", "type@version", "=", "if", "(", "!", "empty", "(", "logAnalytics", ")", ")", "{",
        "key", ":", "value",
        "properties", ":", "{",
        "prop1", ":", "val1",
        "}",
        "}");
  }

  @Test
  void shouldRetrievePropertiesFromForExpression() {
    String code = code("resource myName 'type@version' = [for item in collection: {",
      "key: value",
      "properties: {",
      "prop1: val1",
      "}",
      "}",
      "]");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.existing()).isFalse();

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("resource", "myName", "type@version", "=", "[", "for", "item", "in", "collection", ":", "{",
        "key", ":", "value",
        "properties", ":", "{",
        "prop1", ":", "val1",
        "}",
        "}",
        "]");
  }

  @Test
  void shouldParseResourceDeclarationWithExistingFlag() {
    String code = code("resource myName 'type@version' existing = {",
      "key: value",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    assertThat(tree.existing()).isTrue();
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
      .matches(code("@batchSize(10)",
        "resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {",
        "  ABC: 123",
        "  myKey: myValue",
        "}"))
      .matches(code("@sys.batchSize(10)",
        "@secure()",
        "resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {",
        "  ABC: 123",
        "  myKey: myValue",
        "}"))

      .notMatches(code("resource myName 'type_version' = {",
        "abc",
        "}"));
  }

  @Test
  void shouldProvideTypeAndVersionAsThisForInvalidTypeAndVersion() {
    String code = code("resource myName 'type_version' = {",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("type_version");
    assertThat(tree.version().value()).isEqualTo("type_version");
  }

  @Test
  void shouldProvideTypeAndVersionAsThisForInvalidTypeAndVersion2() {
    String code = code("resource myName 'foo@bar@baz' = {",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("foo@bar@baz");
    assertThat(tree.version().value()).isEqualTo("foo@bar@baz");
  }

  @Test
  void shouldParseResourceDeclarationWithDecorator() {
    String code = code("@foo(10) resource myName 'type@version' = { key: value }");
    ResourceDeclarationImpl tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();

    assertThat(tree.decorators()).hasSize(1);
    Decorator decorator = tree.decorators().get(0);
    Assertions.assertThat(decorator.is(ArmTree.Kind.DECORATOR)).isTrue();
    Assertions.assertThat(decorator.expression().is(ArmTree.Kind.FUNCTION_CALL)).isTrue();
    Assertions.assertThat(decorator.children()).hasSize(2);
  }

  @Test
  void shouldProvideEmptyPropertiesWithForBodyNotObject() {
    String code = code("resource myName 'foo@bar@baz' = [for item in collection: 'value']");
    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.properties()).isEmpty();
  }
}
