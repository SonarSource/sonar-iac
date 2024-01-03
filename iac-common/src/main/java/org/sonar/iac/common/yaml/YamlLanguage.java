/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.common.yaml;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.iac.common.ConfigurationLanguage;

/**
 * This class defines the YAML language.
 */
public class YamlLanguage extends ConfigurationLanguage {

  public static final String KEY = "yaml";
  public static final String NAME = "YAML";
  public static final String YAML_CATEGORY = "YAML";
  public static final String FILE_SUFFIXES_KEY = "sonar.yaml.file.suffixes";
  public static final String DEFAULT_FILE_SUFFIXES = ".yaml,.yml";

  public YamlLanguage(Configuration configuration) {
    super(KEY, NAME, configuration, FILE_SUFFIXES_KEY, DEFAULT_FILE_SUFFIXES);
  }

  public static PropertyDefinition getProperty() {
    return PropertyDefinition.builder(FILE_SUFFIXES_KEY)
      .defaultValue(DEFAULT_FILE_SUFFIXES)
      .name("File Suffixes")
      .description("List of suffixes of YAML files to be indexed.")
      .onQualifiers(Qualifiers.PROJECT)
      .category(YAML_CATEGORY)
      .subCategory("General")
      .multiValues(true)
      .build();
  }

}
