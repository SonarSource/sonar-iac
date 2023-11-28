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
package org.sonarsource.iac;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/properties/";

  @Test
  void testTerraformCustomFileSuffixes() {
    executeBuildAndAssertMetric("terraformCustomFileSuffixes", "terraform", "suffixes", ".terra", "files", 1);
  }

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
    "cloudformationDefaultIdentifier, cloudformation, AWSTemplateFormatVersion, 5",
    "cloudformationCustomIdentifier, cloudformation, CustomIdentifier, 3",
    "cloudformationEmptyIdentifier, cloudformation, '', 8"
  })
  void testCloudformationIdentifier(String projectKey, String language, String identifier, int expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, language, "identifier", identifier, "ncloc", expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "dockerCustomFilenamePatternsDefaultValue;  Dockerfile,*.dockerfile; 4",
    // Empty property defaults to default value
    "dockerCustomFilenamePatternsEmpty; ''; 4",
    "dockerCustomFilenamePatternsWithEmptyValueInside; Dockerfile,  ,*.dockerfile; 4",
    // Dockerfile.* are scanned by default and can't be disabled at the moment
    "dockerCustomFilenamePatternsWithOneElement; customFilename; 2",
    "dockerCustomFilenamePatterns;  Dockerfile,*.dockerfile,*.suffix,customFilename; 6"
  })
  void testDockerFilenamePatterns(String projectKey, String patterns, int expectedFiles) {
    executeBuildAndAssertMetric(projectKey, "docker", "patterns", patterns, "files", expectedFiles);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "jsonDefaultSuffix; json; ''; 9",
    "jsonCustomSuffix; json; .jsn; 9",
    "jsonExtendedSuffix; json; .json,.jsn; 18"
  })
  void testJsonSuffix(String projectKey, String language, String suffixes, int expectedNcloc) {
    // Since json language itself wouldn't publish files, we analyze json files that get picked up by cloudformation sensor
    executeBuildAndAssertMetric(projectKey, language, "suffixes", suffixes, "ncloc", expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "yamlDefaultSuffix; yaml; ''; 10",
    "yamlCustomSuffix; yaml; .rml; 5",
    "yamlExtendedSuffix; yaml; .yml,.yaml,.rml; 15"
  })
  void testYamlSuffix(String projectKey, String language, String suffixes, int expectedNcloc) {
    // Since yaml lanuage itself wouldn't publish files, we analyze yaml files that get picked up by cloudformation sensor
    executeBuildAndAssertMetric(projectKey, language, "suffixes", suffixes, "ncloc", expectedNcloc);
  }

  private void executeBuildAndAssertMetric(
    String projectKey, String language,
    String propertySuffix, String propertyValue,
    String metricKey, int expectedResultOfMetric) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + propertySuffix + "/", language)
      .setProperty("sonar." + language + ".file." + propertySuffix, propertyValue));
    assertThat(getMeasureAsInt(projectKey, metricKey)).isEqualTo(expectedResultOfMetric);
  }

  private void checkTerraformAwsProviderVersion(String projectKey, String version, int expectedHotspots) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "provider/", "terraform", "aws-provider")
      .setProperty("sonar.terraform.provider.aws.version", version));
    assertThat(getHotspotsForProject(projectKey)).hasSize(expectedHotspots);
  }
}
