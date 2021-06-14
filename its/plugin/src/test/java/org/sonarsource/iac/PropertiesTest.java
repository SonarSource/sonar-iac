/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
  public void test_cloudformation_custom_file_suffixes() {
    checkCustomFileSuffixesForLanguage("cloudformationCustomFileSuffixes", "cloudformation", ".cloud", 1);
  }

  @Test
  public void test_terraform_exclusions() {
    checkCustomExclusionsForLanguage("terraformCustomExclusions", "terraform", "**/excludedDir/**", 2);
  }

  @Test
  public void test_cloudformation_exclusions() {
    checkCustomExclusionsForLanguage("cloudformationCustomExclusions", "cloudformation", "**/excludedDir/**", 2);
  }


  @Test
  public void test_cloudformation_identifier() {
    checkCustomFileIdentifierForLanguage("cloudformationDefaultIdentifier", "cloudformation", "AWSTemplateFormatVersion", 3);
    checkCustomFileIdentifierForLanguage("cloudformationCustomIdentifier", "cloudformation", "CustomIdentifier", 1);
    checkCustomFileIdentifierForLanguage("cloudformationEmptyIdentifier", "cloudformation", "", 4);
  }

  private void checkCustomFileSuffixesForLanguage(String projectKey, String language, String suffixes, int expectedFiles) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "file_suffixes/", language)
      .setProperty("sonar." + language + ".file.suffixes", suffixes));
    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
  }

  private void checkCustomExclusionsForLanguage(String projectKey, String language, String exclusions, int expectedFiles) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "exclusions/", language)
      .setProperty("sonar." + language + ".exclusions", exclusions));
    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
  }

  private void checkCustomFileIdentifierForLanguage(String projectKey, String language, String identifier, int expectedFiles) {
    ORCHESTRATOR.executeBuild(getSonarScanner(projectKey, BASE_DIRECTORY + "identifier/", language)
      .setProperty("sonar." + language + ".file.identifier", identifier));
    assertThat(getMeasureAsInt(projectKey, "files")).isEqualTo(expectedFiles);
  }
}
