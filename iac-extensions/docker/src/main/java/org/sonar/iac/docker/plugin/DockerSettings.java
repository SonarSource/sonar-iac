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
package org.sonar.iac.docker.plugin;

import java.util.Arrays;
import java.util.List;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

public class DockerSettings {

  private static final String DOCKER_CATEGORY = "Docker";
  private static final String GENERAL_SUBCATEGORY = "General";
  private static final String VERSION_SUBCATEGORY = "Provider Versions";

  static final String ACTIVATION_KEY = "sonar.docker.activate";
  static final String ACTIVATION_DEFAULT_VALUE = "true";

  static final String FILE_SUFFIXES_KEY = "sonar.docker.file.suffixes";
  static final String FILE_SUFFIXES_DEFAULT_VALUE = "Dockerfile";

  private DockerSettings() {
  }

  public static List<PropertyDefinition> getProperties() {
    return Arrays.asList(
      PropertyDefinition.builder(ACTIVATION_KEY)
        .index(1)
        .defaultValue(ACTIVATION_DEFAULT_VALUE)
        .name("Activate Docker Analysis")
        .description("Disabling Docker analysis ensures that no Docker files are parsed, highlighted and analyzed, " +
          "and no IaC analysis results are included in the quality gate.")
        .type(PropertyType.BOOLEAN)
        .onQualifiers(Qualifiers.PROJECT)
        .category(DOCKER_CATEGORY)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .index(2)
        .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
        .name("File Suffixes")
        .description("List of suffixes of Docker files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category(DOCKER_CATEGORY)
        .multiValues(true)
        .subCategory(GENERAL_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(DockerProviders.Provider.Identifier.AWS.key)
        .index(100)
        .name("AWS Provider Version")
        .description("Version of the AWS provider of lifecycle management of AWS resources. " +
          "Use semantic versioning format like `3.4`, `4.17.1` or `4`")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(DOCKER_CATEGORY)
        .subCategory(VERSION_SUBCATEGORY)
        .build(),

      PropertyDefinition.builder(DockerProviders.Provider.Identifier.AZURE.key)
        .index(101)
        .name("Azure Provider Version")
        .description("Version of the Azure Resource Manager provider of lifecycle management of Microsoft Azure resources. " +
          "Use semantic versioning format like `3.4`, `4.17.1` or `4`")
        .type(PropertyType.STRING)
        .onQualifiers(Qualifiers.PROJECT)
        .category(DOCKER_CATEGORY)
        .subCategory(VERSION_SUBCATEGORY)
        .build()
    );
  }
}
