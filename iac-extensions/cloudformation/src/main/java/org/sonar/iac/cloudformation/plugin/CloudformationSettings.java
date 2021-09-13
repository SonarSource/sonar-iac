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
  private static final String EXTERNAL_ANALYZERS_CATEGORY = "External Analyzers";

  static final String ACTIVATION_KEY = "sonar.cloudformation.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";

  static final String FILE_IDENTIFIER_KEY = "sonar.cloudformation.file.identifier";
  static final String FILE_IDENTIFIER_DEFAULT_VALUE = "AWSTemplateFormatVersion";

  static final String CFN_LINT_REPORTS_KEY = "sonar.cloudformation.cfn-lint.reportPaths";

  private CloudformationSettings() {
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate CloudFormation analysis")
        .description("Activate analysis of JSON and Yaml files recognized as CloudFormation files.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
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
        .build(),

      PropertyDefinition.builder(CFN_LINT_REPORTS_KEY)
        .index(33)
        .name("Cfn-Lint Report Files")
        .description("Paths (absolute or relative) to the files with Cfn-Lint issues.")
        .category(EXTERNAL_ANALYZERS_CATEGORY)
        .subCategory(CLOUDFORMATION_CATEGORY)
        .onQualifiers(Qualifiers.PROJECT)
        .multiValues(true)
        .build());
  }
}
