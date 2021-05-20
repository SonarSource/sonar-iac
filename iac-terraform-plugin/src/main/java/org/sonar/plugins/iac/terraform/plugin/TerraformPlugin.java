/*
 * SonarQube IaC Terraform Plugin
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
package org.sonar.plugins.iac.terraform.plugin;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class TerraformPlugin implements Plugin {

  // Categories
  static final String TERRAFORM_CATEGORY = "Terraform";
  static final String GENERAL_SUBCATEGORY = "General";

  // Global constants
  static final String LANGUAGE_KEY = "terraform";
  static final String LANGUAGE_NAME = "Terraform";
  static final String REPOSITORY_KEY = "terraform";
  static final String REPOSITORY_NAME = "SonarQube";
  static final String PROFILE_NAME = "Sonar way";

  static final String EXCLUSIONS_KEY = "sonar.terraform.exclusions";
  static final String EXCLUSIONS_DEFAULT_VALUE = "";
  static final String FILE_SUFFIXES_DEFAULT_VALUE = ".tf";
  static final String FILE_SUFFIXES_KEY = "sonar.terraform.file.suffixes";

  @Override
  public void define(Context context) {
    context.addExtensions(
      //Language
      TerraformLanguage.class,
      // Sensor
      TerraformSensor.class,
      //Filter
      TerraformExclusionsFileFilter.class,
      // Rules and profiles
      TerraformRulesDefinition.class,
      TerraformProfileDefinition.class,
      // Properties
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
        .name("File Suffixes")
        .description("List of suffixes of Terraform files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(TERRAFORM_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(EXCLUSIONS_KEY)
        .defaultValue(EXCLUSIONS_DEFAULT_VALUE)
        .name("Terraform Exclusions")
        .description("List of file path patterns to be excluded from analysis of Terraform files.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(TERRAFORM_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build()
    );
  }
}
