/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE;
import static org.sonar.iac.common.predicates.CloudFormationFilePredicate.CLOUDFORMATION_FILE_IDENTIFIER_KEY;

public class CloudformationSettings {

  private static final String CLOUDFORMATION_CATEGORY = "CloudFormation";
  private static final String GENERAL_SUBCATEGORY = "General";
  private static final String EXTERNAL_ANALYZERS_CATEGORY = "External Analyzers";

  static final String ACTIVATION_KEY = "sonar.cloudformation.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";

  static final String CFN_LINT_REPORTS_KEY = "sonar.cloudformation.cfn-lint.reportPaths";

  private CloudformationSettings() {
  }

  public static List<PropertyDefinition> getGeneralProperties() {
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

      PropertyDefinition.builder(CLOUDFORMATION_FILE_IDENTIFIER_KEY)
        .index(4)
        .defaultValue(CLOUDFORMATION_FILE_IDENTIFIER_DEFAULT_VALUE)
        .name("File Identifier")
        .description("Files without the identifier are excluded from the analysis. The identifier can be anywhere in the file.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(CLOUDFORMATION_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }

  public static List<PropertyDefinition> getExternalReportProperties() {
    return List.of(PropertyDefinition.builder(CFN_LINT_REPORTS_KEY)
      .index(33)
      .name("Cfn-Lint Report Files")
      .description("Paths (absolute or relative) to the files with Cfn-Lint issues. You can use wildcard patterns to define paths.")
      .category(EXTERNAL_ANALYZERS_CATEGORY)
      .subCategory(CLOUDFORMATION_CATEGORY)
      .onQualifiers(Qualifiers.PROJECT)
      .multiValues(true)
      .build());
  }
}
