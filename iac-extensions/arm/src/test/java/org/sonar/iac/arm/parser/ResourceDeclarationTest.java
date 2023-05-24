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
package org.sonar.iac.arm.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
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
    assertThat(tree.statements().get(0).is(ArmTree.Kind.RESOURCE_DECLARATION)).isTrue();
    assertThat(tree.statements().get(0).is(ArmTree.Kind.EXPRESSION)).isFalse();

    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) tree.statements().get(0);
    assertThat(resourceDeclaration.type()).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(resourceDeclaration.version()).isEqualTo("2022-12-29");

    Expression name = resourceDeclaration.name();
    assertThat(name.value()).isEqualTo("myResource");
    assertThat(name.is(ArmTree.Kind.EXPRESSION)).isTrue();
    IacCommonAssertions.assertThat(name.textRange()).hasRange(6, 14, 6, 26);

    List<Property> properties = resourceDeclaration.properties();
    assertThat(properties).hasSize(1);
    assertThat(properties.get(0).key().value()).isEqualTo("location");
    assertThat(properties.get(0).value().value()).isEqualTo("random location");
    IacCommonAssertions.assertThat(properties.get(0).textRange()).hasRange(7, 6, 7, 35);
  }

  @Test
  void checkChildren() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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
    ResourceDeclaration resourceDeclaration = (ResourceDeclaration) tree.statements().get(0);

    List<Tree> children = resourceDeclaration.children();
    assertThat(children).hasSize(8);

    checkElement(children.get(0), ArmTree.Kind.IDENTIFIER, "name", 6, 6, 6, 12);
    checkElement(children.get(1), ArmTree.Kind.EXPRESSION, "myResource", 6, 14, 6, 26);
    checkElement(children.get(2), ArmTree.Kind.IDENTIFIER, "apiVersion", 5, 6, 5, 18);
    checkElement(children.get(3), ArmTree.Kind.EXPRESSION, "2022-12-29", 5, 20, 5, 32);
    checkElement(children.get(4), ArmTree.Kind.IDENTIFIER, "type", 4, 6, 4, 12);
    checkElement(children.get(5), ArmTree.Kind.EXPRESSION, "Microsoft.Kusto/clusters", 4, 14, 4, 40);
    checkElement(children.get(6), ArmTree.Kind.IDENTIFIER, "location", 7, 6, 7, 16);
    checkElement(children.get(7), ArmTree.Kind.EXPRESSION, "random location", 7, 18, 7, 35);
  }

  void checkElement(Tree element, ArmTree.Kind kind, String value, int startLine, int startLineOffset, int endLine, int endLineOffset)
    throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    assertThat(element).isInstanceOf(ArmTree.class);
    ArmTree armTree = (ArmTree) element;
    assertThat(armTree.is(kind)).isTrue();

    Method method = element.getClass().getMethod("value");
    String elementValue = (String) method.invoke(element);
    assertThat(elementValue).isEqualTo(value);

    assertThat(armTree.children()).isEmpty();
    IacCommonAssertions.assertThat(armTree.textRange()).hasRange(startLine, startLineOffset, endLine, endLineOffset);
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
      .hasMessage("Expecting ScalarTree to convert to Expression, got SequenceTreeImpl");
  }

  @Test
  void shouldThrowParseExceptionOnIncompleteResource() {
    String[] usecases = new String[] {
      // " ", // surprisingly, this throw a different parseException, apparently a node cannot have empty content
      "                                                    \"type\":\"myType\"",
      "                        \"apiVersion\":\"version\"                     ",
      "                        \"apiVersion\":\"version\", \"type\":\"myType\"",
      "\"name\":\"nameValue\"                                                 ",
      "\"name\":\"nameValue\",                             \"type\":\"myType\"",
      "\"name\":\"nameValue\", \"apiVersion\":\"version\"                     ",
    };

    for (String usecase : usecases) {
      String code = code("{",
        "  \"resources\": [",
        "    {",
        "      " + usecase,
        "    }",
        "  ]",
        "}");
      assertThatThrownBy(() -> parser.parse(code, null))
        .isInstanceOf(ParseException.class)
        .hasMessage("Resource without required field spotted (name, type, apiVersion)");
    }
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
    assertThat(resourceDeclaration1.properties().get(0).value().value()).isEqualTo("value1");

    ResourceDeclaration resourceDeclaration2 = (ResourceDeclaration) tree.statements().get(1);
    assertThat(resourceDeclaration2.type()).isEqualTo("type2");
    assertThat(resourceDeclaration2.version()).isEqualTo("version2");
    assertThat(resourceDeclaration2.name().value()).isEqualTo("name2");
    assertThat(resourceDeclaration2.properties()).hasSize(1);
    assertThat(resourceDeclaration2.properties().get(0).key().value()).isEqualTo("property2");
    assertThat(resourceDeclaration2.properties().get(0).value().value()).isEqualTo("value2");
  }
}
