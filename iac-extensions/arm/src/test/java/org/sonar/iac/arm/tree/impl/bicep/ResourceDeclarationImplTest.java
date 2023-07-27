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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.iac.arm.ArmAssertions;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ObjectExpression;
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
    String code = code("resource mySymbolicName 'type@version' = {",
      "name: 'myName'",
      "key: value",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.name().value()).isEqualTo("myName");
    assertThat(tree.type().value()).isEqualTo("type");
    assertThat(tree.version().value()).isEqualTo("version");

    assertThat(tree.properties()).isEmpty();
    assertThat(tree.existing()).isFalse();

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("resource", "mySymbolicName", "type@version", "=", "{", "name", ":", "myName", "key", ":", "value", "}");
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
    assertThat(tree.name()).as("Name is not set in resource declaration").isNull();
  }

  @Test
  void shouldParseResourceDeclarationObjectAndReadResourceProperties() {
    String code = code("resource myName 'type@version' = { key: value",
      "properties: {",
      "prop1: val1",
      "}",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    Property property1 = tree.resourceProperties().get(0);
    assertThat(property1.key().value()).isEqualTo("key");
    assertThat(property1.value()).asIdentifier().hasValue("value");
    Property property2 = tree.resourceProperties().get(1);
    assertThat(property2.key().value()).isEqualTo("properties");
    assertThat(tree.resourceProperties()).hasSize(2);
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
      .matches("resource abc 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      // defining a resource of name the same as keyword is possible
      .matches("resource for 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource if 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource metadata 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource func 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource param 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource output 'Microsoft.Web/sites@2022-09-01' = { name: value }")

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
    assertThat(tree.version()).isNull();
  }

  @Test
  void shouldProvideTypeAndVersionAsThisForInvalidTypeAndVersion2() {
    String code = code("resource myName 'foo@bar@baz' = {",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("foo@bar@baz");
    assertThat(tree.version()).isNull();
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

  @Test
  void shouldParseNestedResource() {
    String code = code("resource myName 'type1@version1' = {",
      "  resource childResource 'type2@version2' = {",
      "    name: 'name'",
      "  }",
      "}");
    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("type1");
    assertThat(tree.version().value()).isEqualTo("version1");

    assertThat(tree.childResources()).hasSize(1);
    ResourceDeclaration child = tree.childResources().get(0);
    assertThat(child.type().value()).isEqualTo("type2");
    assertThat(child.version().value()).isEqualTo("version2");
  }

  @Test
  void shouldProvideEmptyPropertiesForTernaryExpression() {
    String code = code("resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {",
      "  name: myName",
      "  properties: isPublicCloud ? {",
      "    semanticSearch: 'standard'",
      "  } : {",
      "    semanticSearch: 'private'",
      "  }",
      "}");

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.properties()).isEmpty();
  }

  @Test
  void shouldProvideEmptyPropertiesForIdentifier() {
    String code = code("var myProperties = { property: 'value'}",
      "",
      "resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {",
      "  name: myName",
      "  properties: myProperties",
      "}");

    File tree = parse(code, BicepLexicalGrammar.FILE);
    ResourceDeclaration resource = (ResourceDeclaration) tree.statements().get(1);
    assertThat(resource.properties()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"myName", "condition ? foo : 'bar'", "'my${foo}Name'"})
  void shouldProvideEmptyPropertiesForOtherNameTypes(String nameValue) {
    String code = code("resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {",
      "  name: " + nameValue,
      "}");

    File tree = parse(code, BicepLexicalGrammar.FILE);
    ResourceDeclaration resource = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resource.name()).isNull();
  }

  @Test
  void accessParent() {
    String code = code("resource myName 'type1@version1' = {",
      "  resource childResource 'type2@version2' = {",
      "    name: 'name'",
      "  }",
      "}");
    ResourceDeclaration resourceParent = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(resourceParent.parent()).isNull();
    assertThat(resourceParent.children()).hasSize(5);
    assertThat(((ArmTree) resourceParent.children().get(0)).parent()).isSameAs(resourceParent);
    assertThat(((ArmTree) resourceParent.children().get(1)).parent()).isSameAs(resourceParent);
    assertThat(((ArmTree) resourceParent.children().get(2)).parent()).isSameAs(resourceParent);
    assertThat(((ArmTree) resourceParent.children().get(3)).parent()).isSameAs(resourceParent);
    assertThat(((ArmTree) resourceParent.children().get(4)).parent()).isSameAs(resourceParent);

    ObjectExpression propertiesObject = (ObjectExpression) resourceParent.children().get(4);
    ResourceDeclaration resourceChild = resourceParent.childResources().get(0);
    assertThat(resourceChild.parent()).isSameAs(propertiesObject);
    assertThat(propertiesObject.parent()).isSameAs(resourceParent);
  }
}
