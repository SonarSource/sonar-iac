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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesTest extends AbstractPropertiesTest {

  private static final String BASE_DIRECTORY = "projects/properties/";

  @ParameterizedTest
  @CsvSource({
    "terraformAwsProviderVersion3, 3, 1",
    "terraformAwsProviderVersion4, 4, 0",
    "terraformAwsProviderVersionNotProvided, '', 0"
  })
  void testTerraformAwsProviderVersion(String projectKey, String version, int expectedHotspots) {
    checkTerraformAwsProviderVersion(projectKey, version, expectedHotspots);
  }

  @ParameterizedTest
  @CsvSource({
    "cloudformationDefaultIdentifier, AWSTemplateFormatVersion, 3, 5",
    "cloudformationCustomIdentifier, CustomIdentifier, 1, 3",
    "cloudformationEmptyIdentifier, '', 4, 8"
  })
  void testCloudformationIdentifier(String projectKey, String identifier, int expectedFiles, int expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, "cloudformation", "identifier", "sonar.cloudformation.file.identifier", identifier, null, expectedFiles, expectedNcloc);
  }

  private void checkTerraformAwsProviderVersion(String projectKey, String version, int expectedHotspots) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "provider/", "terraform", "aws-provider")
      .setProperty("sonar.terraform.provider.aws.version", version));
    assertThat(getHotspotsForProject(projectKey)).hasSize(expectedHotspots);
  }
}
