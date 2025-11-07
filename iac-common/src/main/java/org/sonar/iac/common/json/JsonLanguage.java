/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.json;

import org.sonar.api.config.Configuration;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.config.PropertyDefinition.ConfigScope;
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
  public static final String ACTIVATION_KEY = "sonar.json.activate";

  public JsonLanguage(Configuration configuration) {
    super(KEY, NAME, configuration, FILE_SUFFIXES_KEY, DEFAULT_FILE_SUFFIXES);
  }

  public static PropertyDefinition getProperty() {
    return PropertyDefinition.builder(FILE_SUFFIXES_KEY)
      .defaultValue(DEFAULT_FILE_SUFFIXES)
      .name("File Suffixes")
      .description("List of suffixes of JSON files to be indexed.")
      .onConfigScopes(ConfigScope.PROJECT)
      .category(JSON_CATEGORY)
      .subCategory("General")
      .multiValues(true)
      .build();
  }

}
