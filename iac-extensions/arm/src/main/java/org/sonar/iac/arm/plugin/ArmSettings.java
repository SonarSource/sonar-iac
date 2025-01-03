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
package org.sonar.iac.arm.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class ArmSettings {

  private static final String ARM_CATEGORY = "AzureResourceManager";
  private static final String GENERAL_SUBCATEGORY = "General";
  protected static final String PROPERTY_KEY_PREFIX = "sonar." + ArmLanguage.KEY;

  protected static final String ACTIVATION_KEY = PROPERTY_KEY_PREFIX + ".activate";
  protected static final String ACTIVATION_DEFAULT_VALUE = "true";
  protected static final String FILE_SUFFIXES_KEY = PROPERTY_KEY_PREFIX + ".file.suffixes";
  protected static final String FILE_SUFFIXES_DEFAULT_VALUE = ".bicep";
  protected static final String FILE_IDENTIFIER_KEY = PROPERTY_KEY_PREFIX + ".file.identifier";
  protected static final String FILE_IDENTIFIER_DEFAULT_VALUE = "https://schema.management.azure.com/schemas/,http://schema.management.azure.com/schemas/";

  private ArmSettings() {
  }

  public static List<PropertyDefinition> getGeneralProperties() {
    return Arrays.asList(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate AzureResourceManager analysis")
        .description("Activate analysis of JSON and Bicep files recognized as ARM files.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(ARM_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .deprecatedKey("sonar.arm.activate")
        .build(),

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .index(2)
        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
        .name("File Suffixes")
        .description("List of suffixes of AzureResourceManager files to analyze next to JSON.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(ARM_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_IDENTIFIER_KEY)
        .index(4)
        .defaultValue(FILE_IDENTIFIER_DEFAULT_VALUE)
        .name("JSON Template File Identifiers")
        .description("ARM JSON templates without any of the identifiers are excluded from the analysis. " +
          "The identifier can be anywhere in the file. " +
          "Only one identifier of the list needs to be matched for the file to be included.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(ARM_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .deprecatedKey("sonar.arm.file.identifier")
        .build());
  }
}
