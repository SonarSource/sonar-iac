/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.checks.azure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AzureBuiltInRolesTest {

  @Test
  void shouldResolveNameFromId() {
    assertThat(AzureBuiltInRoles.nameForId("ba92f5b4-2d11-453d-a403-e96b0029c9fe")).contains("Storage Blob Data Contributor");
    // Lookup is case-insensitive on the GUID
    assertThat(AzureBuiltInRoles.nameForId("BA92F5B4-2D11-453D-A403-E96B0029C9FE")).contains("Storage Blob Data Contributor");
    assertThat(AzureBuiltInRoles.nameForId("11111111-1111-1111-1111-111111111111")).isEmpty();
  }

  @Test
  void shouldResolveIdFromName() {
    assertThat(AzureBuiltInRoles.idForName("Reader")).contains("acdd72a7-3385-48ef-bd42-f606fba81ae7");
    // Azure treats role names case-insensitively
    assertThat(AzureBuiltInRoles.idForName("reader")).contains("acdd72a7-3385-48ef-bd42-f606fba81ae7");
    assertThat(AzureBuiltInRoles.idForName("My Custom Role")).isEmpty();
  }

  @Test
  void shouldReportWhetherBuiltIn() {
    assertThat(AzureBuiltInRoles.isBuiltInId("b24988ac-6180-42a0-ab88-20f7382dd24c")).isTrue();
    assertThat(AzureBuiltInRoles.isBuiltInId("11111111-1111-1111-1111-111111111111")).isFalse();
    assertThat(AzureBuiltInRoles.isBuiltInName("Contributor")).isTrue();
    assertThat(AzureBuiltInRoles.isBuiltInName("My Custom Role")).isFalse();
  }

  @Test
  void shouldNormalizeId() {
    assertThat(AzureBuiltInRoles.normalizeId("ba92f5b4-2d11-453d-a403-e96b0029c9fe")).contains("ba92f5b4-2d11-453d-a403-e96b0029c9fe");
    assertThat(AzureBuiltInRoles.normalizeId("/providers/Microsoft.Authorization/roleDefinitions/BA92F5B4-2D11-453D-A403-E96B0029C9FE"))
      .contains("ba92f5b4-2d11-453d-a403-e96b0029c9fe");
    assertThat(AzureBuiltInRoles.normalizeId("not-a-guid")).isEmpty();
    assertThat(AzureBuiltInRoles.normalizeId(null)).isEmpty();
  }
}
