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
package org.sonar.iac.arm.tree.impl.json;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.ArrayExpression;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.ObjectExpression;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.testing.IacCommonAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.ARRAY_EXPRESSION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.IDENTIFIER;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.OBJECT_EXPRESSION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.OUTPUT_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.STRING_LITERAL;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceDeclarationImplTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseResource() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\"",
      "    }",
      "  ]",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(OUTPUT_DECLARATION)).isFalse();

    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resourceDeclaration.type()).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(resourceDeclaration.version()).isEqualTo("2022-12-29");

    assertThat(resourceDeclaration.name())
      .hasKind(STRING_LITERAL)
      .hasValue("myResource")
      .hasRange(6, 14, 6, 26);

    assertThat(resourceDeclaration.properties()).isEmpty();

    List<Tree> children = resourceDeclaration.children();
    assertThat(children).hasSize(6);

    assertThat((ArmTree) children.get(0)).is(IDENTIFIER).has("value", "name").hasRange(6, 6, 6, 12);
    assertThat((ArmTree) children.get(1)).is(STRING_LITERAL).has("value", "myResource").hasRange(6, 14, 6, 26);
    assertThat((ArmTree) children.get(2)).is(IDENTIFIER).has("value", "apiVersion").hasRange(5, 6, 5, 18);
    assertThat((ArmTree) children.get(3)).is(STRING_LITERAL).has("value", "2022-12-29").hasRange(5, 20, 5, 32);
    assertThat((ArmTree) children.get(4)).is(IDENTIFIER).has("value", "type").hasRange(4, 6, 4, 12);
    assertThat((ArmTree) children.get(5)).is(STRING_LITERAL).has("value", "Microsoft.Kusto/clusters").hasRange(4, 14, 4, 40);
  }

  @ParameterizedTest
  @CsvSource(delimiterString = ";", value = {
    "\"type\": 5,                            \"apiVersion\": \"2022-12-29\", \"name\": \"myResource\"",
    "\"type\": \"Microsoft.Kusto/clusters\", \"apiVersion\": 5,              \"name\": \"myResource\"",
    "\"type\": \"Microsoft.Kusto/clusters\", \"apiVersion\": \"2022-12-29\", \"name\": 5             ",
  })
  void shouldFailOnInvalidPropertyValueType(String invalidPropertyType) {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      invalidPropertyType,
      "    }",
      "  ]",
      "}");

    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessageContainingAll("Expecting", "got", "instead");
  }

  @Test
  void shouldParseResourceWithExtraProperties() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\",",
      "      \"other properties 1\": {\"obj\": \"random location\"},",
      "      \"other properties 2\": [\"val\"]",
      "    }",
      "  ]",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(RESOURCE_DECLARATION)).isTrue();

    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) tree.statements().get(0);
    List<Property> properties = resourceDeclaration.properties();
    assertThat(properties).hasSize(2);

    assertThat(properties.get(0).key().value()).isEqualTo("other properties 1");
    assertThat(properties.get(0).value().is(OBJECT_EXPRESSION)).isTrue();
    ObjectExpression objExpression = (ObjectExpression) properties.get(0).value();
    assertThat(objExpression.properties()).hasSize(1);
    Expression objValue = objExpression.getPropertyByName("obj").value();
    assertThat(objValue.is(STRING_LITERAL)).isTrue();
    assertThat(((StringLiteral) objValue).value()).isEqualTo("random location");
    assertThat(objValue).isLiteral().hasValue("random location");

    assertThat(properties.get(1).key().value()).isEqualTo("other properties 2");
    assertThat(properties.get(1).value().is(ARRAY_EXPRESSION)).isTrue();
    ArrayExpression arrayExpression = (ArrayExpression) properties.get(1).value();
    assertThat(arrayExpression.elements()).hasSize(1);
    assertThat(arrayExpression.children()).hasSize(1);
    Expression arrValue = arrayExpression.elements().get(0);
    assertThat(arrValue.is(STRING_LITERAL)).isTrue();
    assertThat(arrValue).hasValue("val");

    IacCommonAssertions.assertThat(properties.get(0).textRange()).hasRange(7, 6, 7, 53);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    // " ", // surprisingly, this throw a different parseException, apparently a node cannot have empty content
    "                                                    \"type\":\"myType\"; apiVersion",
    "                        \"apiVersion\":\"version\"                     ; type",
    "                        \"apiVersion\":\"version\", \"type\":\"myType\"; name",
    "\"name\":\"nameValue\"                                                 ; type",
    "\"name\":\"nameValue\",                             \"type\":\"myType\"; apiVersion",
    "\"name\":\"nameValue\", \"apiVersion\":\"version\"                     ; type",
  })
  void shouldThrowParseExceptionOnIncompleteResource(String attributes, String errorMessageComponents) {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      " + attributes,
      "    }",
      "  ]",
      "}");
    ParseException parseException = catchThrowableOfType(() -> parser.parse(code, null), ParseException.class);
    assertThat(parseException).hasMessage("Missing mandatory attribute '" + errorMessageComponents + "' at 3:4");
    assertThat(parseException.getDetails()).isNull();
    assertThat(parseException.getPosition().line()).isEqualTo(3);
    assertThat(parseException.getPosition().lineOffset()).isEqualTo(4);
  }

  @Test
  void shouldParseMultipleResources() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"type1\",",
      "      \"apiVersion\": \"version1\",",
      "      \"name\": \"name1\",",
      "      \"property1\": \"value1\",",
      "    },",
      "    {",
      "      \"type\": \"type2\",",
      "      \"apiVersion\": \"version2\",",
      "      \"name\": \"name2\",",
      "      \"property2\": \"value2\",",
      "    }",
      "  ]",
      "}");

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(2);
    assertThat(tree.statements().get(0)).isInstanceOf(ResourceDeclaration.class);
    assertThat(tree.statements().get(1)).isInstanceOf(ResourceDeclaration.class);

    ResourceDeclaration resourceDeclaration1 = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resourceDeclaration1.type()).isEqualTo("type1");
    assertThat(resourceDeclaration1.version()).isEqualTo("version1");
    assertThat(resourceDeclaration1.name()).hasValue("name1");
    assertThat(resourceDeclaration1.properties()).hasSize(1);
    assertThat(resourceDeclaration1.properties().get(0).key().value()).isEqualTo("property1");
    assertThat(resourceDeclaration1.properties().get(0).value().is(STRING_LITERAL)).isTrue();
    assertThat(resourceDeclaration1.properties().get(0).value()).hasValue("value1");

    ResourceDeclaration resourceDeclaration2 = (ResourceDeclaration) tree.statements().get(1);
    assertThat(resourceDeclaration2.type()).isEqualTo("type2");
    assertThat(resourceDeclaration2.version()).isEqualTo("version2");
    assertThat(resourceDeclaration2.name()).hasValue("name2");
    assertThat(resourceDeclaration2.properties()).hasSize(1);
    assertThat(resourceDeclaration2.properties().get(0).key().value()).isEqualTo("property2");
    assertThat(resourceDeclaration2.properties().get(0).value().is(STRING_LITERAL)).isTrue();
    assertThat(resourceDeclaration2.properties().get(0).value()).hasValue("value2");
  }

  @Test
  void shouldParseResourceWithComplexProperties() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"name\": \"test with complex properties\",",
      "      \"type\": \"Microsoft.Network/networkSecurityGroups/securityRules\",",
      "      \"apiVersion\": \"2022-11-01\",",
      "      \"properties\": {",
      "        \"sourceAddressPrefixes\": [\"0.0.0.0/0\"]",
      "      }",
      "    }",
      "  ]",
      "}");

    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0)).isInstanceOf(ResourceDeclaration.class);

    ResourceDeclaration resource = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resource.name()).hasValue("test with complex properties");
    assertThat(resource.type()).isEqualTo("Microsoft.Network/networkSecurityGroups/securityRules");
    assertThat(resource.version()).isEqualTo("2022-11-01");

    assertThat(resource.properties()).hasSize(1);
    Property property = resource.properties().get(0);
    assertThat(property.key().value()).isEqualTo("properties");
    assertThat(property.value()).hasKind(OBJECT_EXPRESSION);

    ObjectExpression objectExpression = (ObjectExpression) property.value();
    assertThat(objectExpression.getMapRepresentation()).hasSize(1);

    Property sourceAddressPrefixesProperty = objectExpression.getPropertyByName("sourceAddressPrefixes");
    assertThat(sourceAddressPrefixesProperty.key().value()).isEqualTo("sourceAddressPrefixes");
    assertThat(sourceAddressPrefixesProperty.value()).hasKind(ARRAY_EXPRESSION);

    ArrayExpression arrayExpression = (ArrayExpression) sourceAddressPrefixesProperty.value();
    assertThat(arrayExpression).isNotNull();
    assertThat(arrayExpression.elements()).hasSize(1);

    Expression value = arrayExpression.elements().get(0);
    assertThat(value).hasKind(STRING_LITERAL).hasValue("0.0.0.0/0");
  }
}
