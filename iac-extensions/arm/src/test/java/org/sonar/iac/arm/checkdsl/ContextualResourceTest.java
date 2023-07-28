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
package org.sonar.iac.arm.checkdsl;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.ArmTestUtils;
import org.sonar.iac.arm.parser.BicepParser;
import org.sonar.iac.arm.parser.bicep.BicepLexicalGrammar;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.arm.ArmTestUtils.CTX;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ContextualResourceTest {

  static ResourceDeclaration SIMPLE_RESOURCE_DECL = ArmTestUtils.parseResource(code(
    "{",
    "  \"type\": \"Microsoft.Kusto/clusters\",",
    "  \"apiVersion\": \"2022-12-29\",",
    "  \"name\": \"myResource\"",
    "}"));
  static ResourceDeclaration RESOURCE_DECL_NO_VERSION = (ResourceDeclaration) BicepParser.create(BicepLexicalGrammar.RESOURCE_DECLARATION)
    .parse("resource mySymbolicName 'type' = { name : 'myName' }", null);

  @Test
  void createCtFromResourceDeclaration() {
    ContextualResource contextualResource = ContextualResource.fromPresent(CTX, SIMPLE_RESOURCE_DECL);

    assertThat(contextualResource.name).isEqualTo("myResource");
    assertThat(contextualResource.type).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(contextualResource.version).isEqualTo("2022-12-29");
  }

  @Test
  void createCtFromResourceDeclarationWithoutVersion() {
    ContextualResource contextualResource = ContextualResource.fromPresent(CTX, RESOURCE_DECL_NO_VERSION);

    assertThat(contextualResource.name).isEqualTo("myName");
    assertThat(contextualResource.type).isEqualTo("type");
    assertThat(contextualResource.version).isEmpty();
  }
}
