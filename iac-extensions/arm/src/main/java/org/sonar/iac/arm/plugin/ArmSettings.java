/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.arm.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class ArmSettings {

  private static final String ARM_CATEGORY = "AzureResourceManager";
  private static final String GENERAL_SUBCATEGORY = "General";

  static final String ACTIVATION_KEY = "sonar.arm.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";

  static final String FILE_IDENTIFIER_KEY = "sonar.arm.file.identifier";
  static final String FILE_IDENTIFIER_DEFAULT_VALUE = "https://schema.management.azure.com/schemas/";

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
        .build(),

      PropertyDefinition.builder(FILE_IDENTIFIER_KEY)
        .index(4)
        .defaultValue(FILE_IDENTIFIER_DEFAULT_VALUE)
        .name("JSON Template File Identifier")
        .description("ARM JSON templates without the identifier are excluded from the analysis. The identifier can be anywhere in the file.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(ARM_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build());
  }
}
