/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class CloudformationSettings {

  private static final String CLOUDFORMATION_CATEGORY = "CloudFormation";
  private static final String GENERAL_SUBCATEGORY = "General";

  static final String ACTIVATION_KEY = "sonar.cloudformation.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "false";

  static final String EXCLUSIONS_KEY = "sonar.cloudformation.exclusions";
  static final String EXCLUSIONS_DEFAULT_VALUE = "";

  static final String FILE_SUFFIXES_KEY = "sonar.cloudformation.file.suffixes";
  static final String FILE_SUFFIXES_DEFAULT_VALUE = ".json,.yaml,.yml";

  static final String FILE_IDENTIFIER_KEY = "sonar.cloudformation.file.identifier";
  static final String FILE_IDENTIFIER_DEFAULT_VALUE = "AWSTemplateFormatVersion";

  private CloudformationSettings() {
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate CloudFormation analysis")
        .description("Activate analysis of JSON and Yaml files recognized as Cloudformation files.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .index(2)
        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
        .name("File Suffixes")
        .description("List of suffixes of Cloudformation files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(EXCLUSIONS_KEY)
        .index(3)
        .defaultValue(EXCLUSIONS_DEFAULT_VALUE)
        .name("CloudFormation Exclusions")
        .description("List of file path patterns to be excluded from analysis of Cloudformation files.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_IDENTIFIER_KEY)
        .index(4)
        .defaultValue(FILE_IDENTIFIER_DEFAULT_VALUE)
        .name("File Identifier")
        .description("Files without the identifier are excluded from the analysis. The identifier can be anywhere in the file.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build()
    );
  }
}
