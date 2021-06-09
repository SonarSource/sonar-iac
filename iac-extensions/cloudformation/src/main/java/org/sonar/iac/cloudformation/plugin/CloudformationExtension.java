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
package org.sonar.iac.cloudformation.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class CloudformationExtension {

  // Categories
  public static final String CLOUDFORMATION_CATEGORY = "Cloudformation";
  public static final String GENERAL_SUBCATEGORY = "General";

  // Global constants
  public static final String LANGUAGE_KEY = "cloudformation";
  public static final String LANGUAGE_NAME = "Cloudformation";
  public static final String REPOSITORY_KEY = "cloudformation";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String PROFILE_NAME = "Sonar way";

  public static final String EXCLUSIONS_KEY = "sonar.cloudformation.exclusions";
  public static final String EXCLUSIONS_DEFAULT_VALUE = "";
  public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".json,.yaml,.yml";
  public static final String FILE_SUFFIXES_KEY = "sonar.cloudformation.file.suffixes";

  public static final String FILE_IDENTIFIER_DEFAULT_VALUE = "AWSTemplateFormatVersion";
  public static final String FILE_IDENTIFIER_KEY = "sonar.cloudformation.file.identifier";

  private static final List<Object> EXTENSIONS = Arrays.asList(
    //Language
    CloudformationLanguage.class,
    // Sensor
    CloudformationSensor.class,
    //Filter
    CloudformationExclusionsFileFilter.class,
    // Rules and profiles
    CloudformationRulesDefinition.class,
    CloudformationProfileDefinition.class,
    // Properties
    PropertyDefinition.builder(CloudformationExtension.FILE_SUFFIXES_KEY)
      .defaultValue(CloudformationExtension.FILE_SUFFIXES_DEFAULT_VALUE)
      .name("File Suffixes")
      .description("List of suffixes of Cloudformation files to analyze.")
      .onQualifiers(Qualifiers.PROJECT)
      .category(CloudformationExtension.CLOUDFORMATION_CATEGORY)
      .multiValues(true)
      .subCategory(CloudformationExtension.GENERAL_SUBCATEGORY)
      .build(),

    PropertyDefinition.builder(CloudformationExtension.EXCLUSIONS_KEY)
      .defaultValue(CloudformationExtension.EXCLUSIONS_DEFAULT_VALUE)
      .name("Cloudformation Exclusions")
      .description("List of file path patterns to be excluded from analysis of Cloudformation files.")
      .onQualifiers(Qualifiers.PROJECT)
      .category(CloudformationExtension.CLOUDFORMATION_CATEGORY)
      .multiValues(true)
      .subCategory(CloudformationExtension.GENERAL_SUBCATEGORY)
      .build(),

    PropertyDefinition.builder(CloudformationExtension.FILE_IDENTIFIER_KEY)
      .defaultValue(CloudformationExtension.FILE_IDENTIFIER_DEFAULT_VALUE)
      .name("File Identifier")
      .description("Files without the identifier are excluded from the analysis. The identifier can be anywhere in the file.")
      .onQualifiers(Qualifiers.PROJECT)
      .category(CloudformationExtension.CLOUDFORMATION_CATEGORY)
      .subCategory(CloudformationExtension.GENERAL_SUBCATEGORY)
      .build()
  );

  private CloudformationExtension() {
  }

  public static List<Object> getExtensions() {
    return EXTENSIONS;
  }
}
