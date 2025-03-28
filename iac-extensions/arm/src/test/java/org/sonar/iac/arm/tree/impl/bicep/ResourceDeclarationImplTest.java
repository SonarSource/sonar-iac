/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
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
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.arm.tree.api.bicep.Decorator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.recursiveTransformationOfTreeChildrenToStrings;

class ResourceDeclarationImplTest extends BicepTreeModelTest {

  @Test
  void shouldParseResourceDeclarationObject() {
    var code = """
      resource mySymbolicName 'type@version' = {
        name: 'myName'
        key: value
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();
    assertThat(((StringLiteral) tree.name()).value()).isEqualTo("myName");
    assertThat(tree.type().value()).isEqualTo("type");
    assertThat(((StringLiteral) tree.version()).value()).isEqualTo("version");

    assertThat(tree.properties()).isEmpty();
    assertThat(tree.existing()).isNull();

    assertThat(recursiveTransformationOfTreeChildrenToStrings(tree))
      .containsExactly("resource", "mySymbolicName", "type@version", "=", "{", "name", ":", "myName", "key", ":", "value", "}");
  }

  @Test
  void shouldParseResourceDeclarationObjectAndReadProperties() {
    var code = """
      resource myName 'type@version' = { key: value
        properties: {
          prop1: val1
        }
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asWrappedIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.name()).as("Name is not set in resource declaration").isNull();
  }

  @Test
  void shouldParseResourceDeclarationObjectAndReadResourceProperties() {
    var code = """
      resource myName 'type@version' = { key: value
        properties: {
          prop1: val1
        }
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    Property property1 = tree.resourceProperties().get(0);
    assertThat(property1.key().value()).isEqualTo("key");
    assertThat(property1.value()).asWrappedIdentifier().hasValue("value");
    Property property2 = tree.resourceProperties().get(1);
    assertThat(property2.key().value()).isEqualTo("properties");
    assertThat(tree.resourceProperties()).hasSize(2);
  }

  @Test
  void shouldRetrievePropertiesFromIfCondition() {
    var code = """
      resource myName 'type@version' = if (!empty(logAnalytics)) {
        key: value
        properties: {
          prop1: val1
        }
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asWrappedIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.existing()).isNull();

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
    var code = """
      resource myName 'type@version' = [for item in collection: {
        key: value
        properties: {
          prop1: val1
        }
      }
      ]""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();

    Property property = tree.properties().get(0);
    assertThat(property.key().value()).isEqualTo("prop1");
    assertThat(property.value()).asWrappedIdentifier().hasValue("val1");
    assertThat(tree.properties()).hasSize(1);
    assertThat(tree.existing()).isNull();

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
    var code = """
      resource myName 'type@version' existing = {
        key: value
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);

    assertThat(tree.existing()).isNotNull();
    assertThat(tree.getKind()).isEqualTo(ArmTree.Kind.RESOURCE_DECLARATION_EXISTING);
  }

  @Test
  void shouldParseResourceDeclaration() {
    ArmAssertions.assertThat(BicepLexicalGrammar.RESOURCE_DECLARATION)
      .matches("""
        resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {
          name: workloadIpGroupName
          location: location1
        }""")
      .matches("""
        resource workloadIpGroup 'Microsoft.Network/ipGroups@2022-01-01' = {
          name: workloadIpGroupName
          location: location1
        }""")
      .matches("""
        resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' = {
          ABC: 123
          myKey: myValue
        }""")
      .matches("""
        resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {
          ABC: 123
          myKey: myValue
        }""")
      .matches("""
        @batchSize(10)
        resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {
          ABC: 123
          myKey: myValue
        }""")
      .matches("""
        @sys.batchSize(10)
        @secure()
        resource a_b_c 'Microsoft.Network/ipGroups@2022-01-01' existing = {
          ABC: 123
          myKey: myValue
        }""")
      .matches("resource abc 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      // defining a resource of name the same as keyword is possible
      .matches("resource for 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource if 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource metadata 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource func 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource param 'Microsoft.Web/sites@2022-09-01' = { name: value }")
      .matches("resource output 'Microsoft.Web/sites@2022-09-01' = { name: value }")

      .notMatches("""
        resource myName 'type_version' = {",
        "abc",
        "}""");
  }

  @Test
  void shouldProvideTypeAndVersionAsThisForInvalidTypeAndVersion() {
    String code = """
      resource myName 'type_version' = {
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("type_version");
    assertThat(tree.version()).isNull();
  }

  @Test
  void shouldProvideTypeAndVersionAsThisForInvalidTypeAndVersion2() {
    String code = """
      resource myName 'foo@bar@baz' = {
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("foo@bar@baz");
    assertThat(tree.version()).isNull();
  }

  @Test
  void shouldParseResourceDeclarationWithDecorator() {
    String code = "@foo(10) resource myName 'type@version' = { key: value }";
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
    String code = "resource myName 'foo@bar@baz' = [for item in collection: 'value']";
    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.properties()).isEmpty();
  }

  @Test
  void shouldParseNestedResource() {
    String code = """
      resource myName 'type1@version1' = {
        resource childResource 'type2@version2' = {
          name: 'name'
        }
      }""";
    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.type().value()).isEqualTo("type1");
    assertThat(((StringLiteral) tree.version()).value()).isEqualTo("version1");

    assertThat(tree.childResources()).hasSize(1);
    ResourceDeclaration child = tree.childResources().get(0);
    assertThat(child.type().value()).isEqualTo("type2");
    assertThat(((StringLiteral) child.version()).value()).isEqualTo("version2");
  }

  @Test
  void shouldProvideEmptyPropertiesForTernaryExpression() {
    String code = """
      resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {
        name: myName
        properties: isPublicCloud ? {
          semanticSearch: 'standard'
        } : {
          semanticSearch: 'private'
        }
      }""";

    ResourceDeclaration tree = parse(code, BicepLexicalGrammar.RESOURCE_DECLARATION);
    assertThat(tree.properties()).isEmpty();
  }

  @Test
  void shouldProvideEmptyPropertiesForIdentifier() {
    String code = """
      var myProperties = { property: 'value'}

      resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {
        name: myName
        properties: myProperties
      }""";

    File tree = parse(code, BicepLexicalGrammar.FILE);
    ResourceDeclaration resource = (ResourceDeclaration) tree.statements().get(1);
    assertThat(resource.properties()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"myName", "condition ? foo : 'bar'", "'my${foo}Name'"})
  void shouldProvideEmptyPropertiesForOtherNameTypes(String nameValue) {
    String code = """
      resource myResource 'Microsoft.Search/searchServices@2021-04-01-Preview' = {
        name: %s
      }""".formatted(nameValue);

    File tree = parse(code, BicepLexicalGrammar.FILE);
    ResourceDeclaration resource = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resource.name()).isNull();
  }

  @Test
  void accessParent() {
    String code = """
      resource myName 'type1@version1' = {
        resource childResource 'type2@version2' = {
          name: 'name'
        }
      }""";
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
