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
package org.sonarsource.iac.properties;

import com.sonar.orchestrator.build.SonarScanner;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class ArmSuffixesTest extends AbstractPropertiesTest {
  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "armDefaultSuffixNoProvidedProperty; ; 1; 22",
    "armDefaultSuffixEmptyPropertyValue; '';;",
    "armCustomSuffix; .bicep; 1; 22",
    "armExtendedSuffix; .bicep,.tricep; 2; 44"
  })
  void testArmSuffix(String projectKey, String suffixes, Integer expectedFiles, Integer expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, "azureresourcemanager", "suffixes", "sonar.azureresourcemanager.file.suffixes", suffixes, null, expectedFiles, expectedNcloc);
  }

  // The active property will always win over the deprecated one if set
  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "armDeprecatedPropertyTestActivePropertyEnabledDeprecatedPropertyDisabled; false; true; 22",
    "armDeprecatedPropertyTestDeprecatedPropertyEnabledActivePropertyDisabled; true; false;",
    "armDeprecatedPropertyTestOnlyActivePropertyDisabled;; false;",
    "armDeprecatedPropertyTestOnlyDeprecatedPropertyDisabled; false;;",
  })
  void testDeprecatedArmActivationPropertyKey(String projectKey, Boolean armPropertyValue, Boolean azureresourcemanagerPropertyValue, Integer expectedNcloc) {
    SonarScanner sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY + "suffixes/", "azureresourcemanager");
    if (armPropertyValue != null) {
      sonarScanner.setProperty("sonar.arm.activate", armPropertyValue.toString());
    }
    if (azureresourcemanagerPropertyValue != null) {
      sonarScanner.setProperty("sonar.azureresourcemanager.activate", azureresourcemanagerPropertyValue.toString());
    }
    ORCHESTRATOR.executeBuild(sonarScanner);
    assertThat(getMeasureAsInt(projectKey, "ncloc")).isEqualTo(expectedNcloc);
  }
}
