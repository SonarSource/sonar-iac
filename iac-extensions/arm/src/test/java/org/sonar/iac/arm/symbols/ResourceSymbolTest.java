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
package org.sonar.iac.arm.symbols;

import org.junit.jupiter.api.Test;
import org.sonar.iac.arm.tree.api.ResourceDeclaration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.sonar.iac.common.testing.IacTestUtils.code;

class ResourceSymbolTest extends AbstractArmSymbolTest {

  static ResourceDeclaration SIMPLE_RESOURCE_DECL = parseResource(code(
    "{",
    "  \"type\": \"Microsoft.Kusto/clusters\",",
    "  \"apiVersion\": \"2022-12-29\",",
    "  \"name\": \"myResource\"",
    "}"));

  @Test
  void createSymbolFromResourceDeclaration() {
    ResourceSymbol symbol = ResourceSymbol.fromPresent(ctx, SIMPLE_RESOURCE_DECL);

    assertThat(symbol.name).isEqualTo("myResource");
    assertThat(symbol.type).isEqualTo("Microsoft.Kusto/clusters");
    assertThat(symbol.version).isEqualTo("2022-12-29");
  }

  @Test
  void raiseExceptionWhenReportIssueOnResource() {
    ResourceSymbol symbol = ResourceSymbol.fromPresent(ctx, SIMPLE_RESOURCE_DECL);

    Exception exception = assertThrows(UnsupportedOperationException.class, () -> symbol.reportIfAbsent("test"));
    assertThat(exception.getMessage()).isEqualTo("Resource symbols should always exists");
  }

}
