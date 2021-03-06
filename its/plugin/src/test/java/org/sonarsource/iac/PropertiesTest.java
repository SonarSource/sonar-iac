/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesTest extends TestBase {

  private static final String BASE_DIRECTORY = "projects/properties/";

  @Test
  public void test_terraform_custom_file_suffixes() {
    checkCustomFileSuffixesForLanguage("terraformCustomFileSuffixes", "terraform", ".terra", 1);
  }

  @Test
  public void test_terraform_aws_provider_version() {
    checkTerraformAwsProviderVersion("terraformAwsProviderVersion3", "3", 1);
    checkTerraformAwsProviderVersion("terraformAwsProviderVersion4", "4", 0);
    checkTerraformAwsProviderVersion("terraformAwsProviderVersionNotProvided", "", 0);
  }

  @Test
  public void test_cloudformation_identifier() {
    checkCustomFileIdentifierForLanguage("cloudformationDefaultIdentifier", "cloudformation", "AWSTemplateFormatVersion", 5);
    checkCustomFileIdentifierForLanguage("cloudformationCustomIdentifier", "cloudformation", "CustomIdentifier", 3);
    checkCustomFileIdentifierForLanguage("cloudformationEmptyIdentifier", "cloudformation", "", 8);
  }

  private void checkCustomFileSuffixesForLanguage(String projectKey, String language, String suffixes, int expectedFiles) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "file_suffixes/", language)
      .setProperty("sonar." + language + ".file.suffixes", suffixes));
    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
  }

  private void checkCustomFileIdentifierForLanguage(String projectKey, String language, String identifier, int expectedNcloc) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "identifier/", language)
      .setProperty("sonar." + language + ".file.identifier", identifier));
    assertThat(getMeasureAsInt(projectKey, "ncloc")).isEqualTo(expectedNcloc);
  }

  private void checkTerraformAwsProviderVersion(String projectKey, String version, int expectedHotspots) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "provider/", "terraform", "aws-provider")
      .setProperty("sonar.terraform.provider.aws.version", version));
    assertThat(getHotspotsForProject(projectKey)).hasSize(expectedHotspots);
  }
}
