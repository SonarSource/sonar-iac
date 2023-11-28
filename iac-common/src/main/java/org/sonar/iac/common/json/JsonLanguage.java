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
package org.sonar.iac.common.json;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.iac.common.ConfigurationLanguage;

/**
 * This class defines the JSON language.
 */
public class JsonLanguage extends ConfigurationLanguage {

  public static final String KEY = "json";
  public static final String NAME = "JSON";
  public static final String JSON_CATEGORY = "JSON";
  public static final String FILE_SUFFIXES_KEY = "sonar.json.file.suffixes";
  public static final String DEFAULT_FILE_SUFFIXES = ".json";

  public JsonLanguage(Configuration configuration) {
    super(KEY, NAME, configuration, DEFAULT_FILE_SUFFIXES);
  }

  public static PropertyDefinition getProperty() {
    return PropertyDefinition.builder(FILE_SUFFIXES_KEY)
      .defaultValue(DEFAULT_FILE_SUFFIXES)
      .name("File Suffixes")
      .description("List of suffixes of JSON files to be indexed.")
      .onQualifiers(Qualifiers.PROJECT)
      .category(JSON_CATEGORY)
      .subCategory("General")
      .multiValues(true)
      .build();
  }

}
