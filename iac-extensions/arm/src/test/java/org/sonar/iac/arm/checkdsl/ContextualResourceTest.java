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
package org.sonar.iac.arm.checkdsl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;
import org.sonar.iac.arm.tree.api.StringLiteral;
import org.sonar.iac.common.checks.TextUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.sonar.iac.arm.ArmTestUtils.CTX;

class ContextualResourceTest {

  static ResourceDeclaration SIMPLE_RESOURCE_DECL = ArmTestUtils.parseResource("""
    {
      "type": "Microsoft.Kusto/clusters",
      "apiVersion": "2022-12-29",
      "name": "myResource"
    }""");
  static ResourceDeclaration RESOURCE_DECL_NO_VERSION = (ResourceDeclaration) BicepParser.create(BicepLexicalGrammar.RESOURCE_DECLARATION)
    .parse("resource mySymbolicName 'type' = { name : 'myName' }", null);
  static ResourceDeclaration RESOURCE_DECL_WITH_NESTED = ArmTestUtils.parseResource("""
    {
      "type": "Microsoft.Kusto/clusters",
      "apiVersion": "2022-12-29",
      "name": "myResource",
      "resources": [
        {
          "type": "config",
          "apiVersion": "2020-12-01",
          "name": "example-config"
        }
      ]
    }""");

  @Test
  void createCtFromResourceDeclaration() {
    ContextualResource contextualResource = ContextualResource.fromPresent(CTX, SIMPLE_RESOURCE_DECL);

    assertThat(contextualResource.name).isEqualTo("myResource");
    assertThat(contextualResource.type).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(((StringLiteral) contextualResource.version).value()).isEqualTo("2022-12-29");
  }

  @Test
  void createCtFromResourceDeclarationWithoutVersion() {
    ContextualResource contextualResource = ContextualResource.fromPresent(CTX, RESOURCE_DECL_NO_VERSION);

    assertThat(contextualResource.name).isEqualTo("myName");
    assertThat(contextualResource.type).isEqualTo("type");
    assertThat(contextualResource.version).isNull();
  }

  @Test
  void createCtFromResourceDeclarationWithNestedResource() {
    ContextualResource contextualResource = ContextualResource.fromPresent(CTX, RESOURCE_DECL_WITH_NESTED);
    ContextualResource nestedResource = contextualResource.childResourceBy("config", it -> TextUtils.isValue(it.name(), "example-config").isTrue());
    ContextualResource absentNestedResource = contextualResource.childResourceBy("nonexistent", it -> true);
    absentNestedResource.reportIfAbsent("Issue on an absent resource");

    assertThat(contextualResource.tree.childResources()).hasSize(1);

    assertThat(nestedResource.name).isEqualTo("example-config");
    assertThat(nestedResource.type).isEqualTo("config");

    Mockito.verify(CTX, Mockito.times(1)).reportIssue(contextualResource.tree.type(), "Issue on an absent resource", List.of());
  }

  @Test
  void shouldFindChildResourcesOutsideOfParentInBicep() {
    // language=bicep
    var code = """
      resource myResource 'parentType' = {
        name: 'parent'
      }
      resource correctTypeWrongName 'parentType/childType' = {
        name: 'unrelated'
      }
      resource myChildResource 'parentType/childType' = {
        name: 'parent/child'
      }
      """;
    var file = ArmTestUtils.parseBicep(code);
    var parent = ContextualResource.fromPresent(CTX, (ResourceDeclaration) file.statements().get(0), "parentType");

    var child = parent.childResourceBy("childType", it -> TextUtils.isValue(it.name(), "parent/child").isTrue());

    assertThat(child)
      .returns(true, from(ContextualResource::isPresent))
      .returns("parent/child", from(it -> it.name))
      .returns("parentType/childType", from(it -> it.type));
  }

  @Test
  void shouldFindChildResourcesOutsideOfParentInJson() {
    // language=json
    var code = """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        "contentVersion": "1.0.0.0",
        "resources": [
          {
            "type": "parentType",
            "name": "parent",
            "apiVersion": "2022-12-29"
          },
          {
            "type": "parentType/childType",
            "name": "parent/child",
            "apiVersion": "2022-12-29"
          }
        ]
      }
      """;
    var file = ArmTestUtils.parseJson(code);
    var parent = ContextualResource.fromPresent(CTX, (ResourceDeclaration) file.statements().get(0), "parentType");

    var child = parent.childResourceBy("childType", it -> TextUtils.isValue(it.name(), "parent/child").isTrue());

    assertThat(child)
      .returns(true, from(ContextualResource::isPresent))
      .returns("parent/child", from(it -> it.name))
      .returns("parentType/childType", from(it -> it.type));
  }

  @Test
  void shouldFindChildResourcesNestedInTheSameParentInBicep() {
    // language=bicep
    var code = """
      resource myResource 'outerType' = {
        name: 'outer'
        resource parent 'parentType' = {
          name: 'parent'
        }
        resource child 'parentType/childType' = {
          name: 'parent/child'
        }
      }
      """;
    var file = ArmTestUtils.parseBicep(code);
    var parent = ContextualResource.fromPresent(CTX, ((ResourceDeclaration) file.statements().get(0)).childResources().get(0), "parentType");

    var child = parent.childResourceBy("childType", it -> TextUtils.isValue(it.name(), "parent/child").isTrue());

    assertThat(child)
      .returns(true, from(ContextualResource::isPresent))
      .returns("parent/child", from(it -> it.name))
      .returns("parentType/childType", from(it -> it.type));
  }

  @Test
  void shouldFindChildResourcesNestedInTheSameParentInJson() {
    // language=json
    var code = """
      {
        "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentTemplate.json#",
        "contentVersion": "1.0.0.0",
        "resources": [
          {
            "type": "outerType",
            "name": "outer",
            "apiVersion": "2022-12-29",
            "resources": [
              {
              "type": "parentType",
              "name": "parent",
              "apiVersion": "2022-12-29"
              },
              {
                "type": "parentType/childType",
                "name": "parent/child",
                "apiVersion": "2022-12-29"
              }
            ]
          }
        ]
      }""";
    var file = ArmTestUtils.parseJson(code);
    var parent = ContextualResource.fromPresent(CTX, ((ResourceDeclaration) file.statements().get(0)).childResources().get(0), "parentType");

    var child = parent.childResourceBy("childType", it -> TextUtils.isValue(it.name(), "parent/child").isTrue());

    assertThat(child)
      .returns(true, from(ContextualResource::isPresent))
      .returns("parent/child", from(it -> it.name))
      .returns("parentType/childType", from(it -> it.type));
  }
}
