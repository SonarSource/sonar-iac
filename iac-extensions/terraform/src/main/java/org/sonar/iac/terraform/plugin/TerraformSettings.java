/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class TerraformSettings {

  private static final String TERRAFORM_CATEGORY = "Terraform";
  private static final String GENERAL_SUBCATEGORY = "General";

  static final String ACTIVATION_KEY = "sonar.terraform.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";

  static final String FILE_SUFFIXES_KEY = "sonar.terraform.file.suffixes";
  static final String FILE_SUFFIXES_DEFAULT_VALUE = ".tf";

  private TerraformSettings() {
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Terraform analysis")
        .description("Activate analysis of Terraform files.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(TERRAFORM_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .index(2)
        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
        .name("File Suffixes")
        .description("List of suffixes of Terraform files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(TERRAFORM_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build()
    );
  }
}
