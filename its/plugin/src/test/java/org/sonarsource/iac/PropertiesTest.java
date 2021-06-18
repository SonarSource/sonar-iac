/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
