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
package org.sonar.iac.terraform.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class TerraformSettings {

  private static final String TERRAFORM_CATEGORY = "Terraform";
  private static final String GENERAL_SUBCATEGORY = "General";
  private static final String VERSION_SUBCATEGORY = "Provider Versions";

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
        .build(),

      PropertyDefinition.builder(TerraformProviders.Provider.Identifier.AWS.key)
        .index(101)
        .name("AWS Provider Version")
        .description("Version of the AWS provider of lifecycle management of AWS resources," +
          "including EC2, Lambda, EKS, ECS, VPC, S3, RDS, DynamoDB, and more. Use semantic versioning format.")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(TERRAFORM_CATEGORY)
        .subCategory(VERSION_SUBCATEGORY)
        .build()
    );
  }
}
