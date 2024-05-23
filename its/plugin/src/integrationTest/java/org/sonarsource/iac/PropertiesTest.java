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
package org.sonarsource.iac;

import com.sonar.orchestrator.build.SonarScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

class PropertiesTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/properties/";

  @Test
  void testTerraformCustomFileSuffixes() {
    executeBuildAndAssertMetric("terraformCustomFileSuffixes", "terraform", "suffixes", "sonar.terraform.file.suffixes", ".terra", null, 1, 5);
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
    "cloudformationDefaultIdentifier, AWSTemplateFormatVersion, 3, 5",
    "cloudformationCustomIdentifier, CustomIdentifier, 1, 3",
    "cloudformationEmptyIdentifier, '', 4, 8"
  })
  void testCloudformationIdentifier(String projectKey, String identifier, int expectedFiles, int expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, "cloudformation", "identifier", "sonar.cloudformation.file.identifier", identifier, null, expectedFiles, expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "dockerCustomFilenamePatternsDefaultValue;  Dockerfile,*.dockerfile; 4; 4",
    // Empty property defaults to default value
    "dockerCustomFilenamePatternsNoPropertyDefined;; 4; 4",
    // Dockerfile.* are scanned by default, so even with empty patterns we still find one dockerfile
    "dockerCustomFilenamePatternsEmpty; ''; 1; 1",
    "dockerCustomFilenamePatternsWithEmptyValueInside; Dockerfile,  ,*.dockerfile; 4; 4",
    // Dockerfile.* are scanned by default and can't be disabled at the moment
    "dockerCustomFilenamePatternsWithOneElement; customFilename; 2; 2",
    "dockerCustomFilenamePatterns;  Dockerfile,*.dockerfile,*.suffix,customFilename; 6; 6"
  })
  void testDockerFilenamePatterns(String projectKey, String patterns, int expectedFiles, int expectedNcloc) {
    executeBuildAndAssertMetric(projectKey, "docker", "patterns", "sonar.docker.file.patterns", patterns, null, expectedFiles, expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "jsonDefaultSuffixNoProvidedProperty; ; 1; 9",
    "jsonDefaultSuffixEmptyPropertyValue; '';;",
    "jsonCustomSuffix; .jsn; 1; 9",
    "jsonExtendedSuffix; .json,.jsn; 2; 18"
  })
  void testJsonSuffix(String projectKey, String suffixes, Integer expectedFiles, Integer expectedNcloc) {
    // Since json language itself wouldn't publish files, we analyze json files that get picked up by cloudformation sensor
    executeBuildAndAssertMetric(projectKey, "json", "suffixes", "sonar.json.file.suffixes", suffixes, null, expectedFiles, expectedNcloc);
  }

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "yamlDefaultSuffixNoProvidedProperty; ; 2; 10",
    "yamlDefaultSuffixEmptyPropertyValue; '';;",
    "yamlCustomSuffix; .rml; 1; 5",
    "yamlExtendedSuffix; .yml,.yaml,.rml; 3; 15"
  })
  void testYamlSuffix(String projectKey, String suffixes, Integer expectedFiles, Integer expectedNcloc) {
    // Since yaml language itself wouldn't publish files, we analyze yaml files that get picked up by cloudformation sensor
    executeBuildAndAssertMetric(projectKey, "yaml", "suffixes", "sonar.yaml.file.suffixes", suffixes, null, expectedFiles, expectedNcloc);
  }

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

  @ParameterizedTest
  @CsvSource(delimiter = ';', value = {
    "springConfigCustomPattern; application*.properties,config*.properties,*.yaml; 5; 13",
    "springConfigCustomPropertiesPattern; application*.properties,config*.properties; 4; 4",
    "springConfigExcludeBoot; config*.properties; 2; 2",
    "springCustomYaml; *.yaml; 1; 9",
  })
  void testSpringConfigPatterns(String projectKey, String patterns, int expectedFiles, int expectedNCloc) {
    executeBuildAndAssertMetric(projectKey, "java", "patterns", "sonar.java.springconfig.file.patterns", patterns, "springconfig-its", expectedFiles, expectedNCloc);
  }

  private void executeBuildAndAssertMetric(
    String projectKey, String language, String subDir,
    String property, String propertyValue,
    @Nullable String profileName,
    @Nullable Integer expectedFiles, @Nullable Integer expectedNcloc) {
    var sonarScanner = getSonarScanner(projectKey, BASE_DIRECTORY + subDir + "/", language, profileName);
    if (propertyValue != null) {
      sonarScanner.setProperty(property, propertyValue);
    }

    ORCHESTRATOR.executeBuild(sonarScanner);

    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
    assertThat(getMeasureAsInt(projectKey, "ncloc")).isEqualTo(expectedNcloc);
  }

  private void checkTerraformAwsProviderVersion(String projectKey, String version, int expectedHotspots) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "provider/", "terraform", "aws-provider")
      .setProperty("sonar.terraform.provider.aws.version", version));
    assertThat(getHotspotsForProject(projectKey)).hasSize(expectedHotspots);
  }
}
