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
package org.sonar.iac.arm.parser.json;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.sonar.iac.arm.parser.ArmParser;
import org.sonar.iac.arm.tree.api.ArmTree;
import org.sonar.iac.arm.tree.api.Expression;
import org.sonar.iac.arm.tree.api.File;
import org.sonar.iac.arm.tree.api.Property;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.extension.ParseException;
import org.sonar.iac.common.testing.IacCommonAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.sonar.iac.arm.ArmAssertions.assertThat;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.EXPRESSION;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.IDENTIFIER;
import static org.sonar.iac.arm.tree.api.ArmTree.Kind.RESOURCE_DECLARATION;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceDeclarationTest {

  private final ArmParser parser = new ArmParser();

  @Test
  void shouldParseResource() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": \"myResource\",",
      "      \"location\": \"random location\",",
      "    }",
      "  ]",
      "}");
    File tree = (File) parser.parse(code, null);
    assertThat(tree.statements()).hasSize(1);
    assertThat(tree.statements().get(0).is(RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(EXPRESSION)).isFalse();

    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resourceDeclaration.type()).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(resourceDeclaration.version()).isEqualTo("2022-12-29");

    assertThat(resourceDeclaration.name())
      .hasKind(EXPRESSION)
      .hasValue("myResource")
      .hasRange(6, 14, 6, 26);

    List<Property> properties = resourceDeclaration.properties();
    assertThat(properties).hasSize(1);
    assertThat(properties.get(0).key().value()).isEqualTo("location");
    assertThat(properties.get(0).value().is(EXPRESSION)).isTrue();
    assertThat(((Expression) properties.get(0).value()).value()).isEqualTo("random location");
    IacCommonAssertions.assertThat(properties.get(0).textRange()).hasRange(7, 6, 7, 35);

    List<Tree> children = resourceDeclaration.children();
    assertThat(children).hasSize(8);

    assertThat((ArmTree) children.get(0)).is(IDENTIFIER).has("value", "name").hasRange(6, 6, 6, 12);
    assertThat((ArmTree) children.get(1)).is(EXPRESSION).has("value", "myResource").hasRange(6, 14, 6, 26);
    assertThat((ArmTree) children.get(2)).is(IDENTIFIER).has("value", "apiVersion").hasRange(5, 6, 5, 18);
    assertThat((ArmTree) children.get(3)).is(EXPRESSION).has("value", "2022-12-29").hasRange(5, 20, 5, 32);
    assertThat((ArmTree) children.get(4)).is(IDENTIFIER).has("value", "type").hasRange(4, 6, 4, 12);
    assertThat((ArmTree) children.get(5)).is(EXPRESSION).has("value", "Microsoft.Kusto/clusters").hasRange(4, 14, 4, 40);
    assertThat((ArmTree) children.get(6)).is(IDENTIFIER).has("value", "location").hasRange(7, 6, 7, 16);
    assertThat((ArmTree) children.get(7)).is(EXPRESSION).has("value", "random location").hasRange(7, 18, 7, 35);
  }

  @Test
  void invalidNameExpression() {
    String code = code("{",
      "  \"resources\": [",
      "    {",
      "      \"type\": \"Microsoft.Kusto/clusters\",",
      "      \"apiVersion\": \"2022-12-29\",",
      "      \"name\": [],",
      "      \"location\": \"random location\",",
      "    }",
      "  ]",
      "}");
    assertThatThrownBy(() -> parser.parse(code, null))
      .isInstanceOf(ParseException.class)
      .hasMessage("convertToSimpleProperty: Expecting Expression in property value, got ArrayExpressionImpl instead at 6:14");
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
    assertThat(resourceDeclaration1.name().value()).isEqualTo("name1");
    assertThat(resourceDeclaration1.properties()).hasSize(1);
    assertThat(resourceDeclaration1.properties().get(0).key().value()).isEqualTo("property1");
    assertThat(resourceDeclaration1.properties().get(0).value().is(EXPRESSION)).isTrue();
    assertThat(((Expression) resourceDeclaration1.properties().get(0).value()).value()).isEqualTo("value1");

    ResourceDeclaration resourceDeclaration2 = (ResourceDeclaration) tree.statements().get(1);
    assertThat(resourceDeclaration2.type()).isEqualTo("type2");
    assertThat(resourceDeclaration2.version()).isEqualTo("version2");
    assertThat(resourceDeclaration2.name().value()).isEqualTo("name2");
    assertThat(resourceDeclaration2.properties()).hasSize(1);
    assertThat(resourceDeclaration2.properties().get(0).key().value()).isEqualTo("property2");
    assertThat(resourceDeclaration2.properties().get(0).value().is(EXPRESSION)).isTrue();
    assertThat(((Expression) resourceDeclaration2.properties().get(0).value()).value()).isEqualTo("value2");
  }
}
