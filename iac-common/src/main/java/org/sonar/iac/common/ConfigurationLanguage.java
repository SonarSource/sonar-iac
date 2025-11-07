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
package org.sonar.iac.common;

import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines the YAML language.
 */
public class ConfigurationLanguage extends AbstractLanguage {

  private final String fileSuffixesKey;
  private final String defaultFileSuffixes;

  private final Configuration configuration;

  public ConfigurationLanguage(String key, String name, Configuration configuration, String fileSuffixesKey, String defaultSuffixes) {
    super(key, name);
    this.configuration = configuration;
    this.fileSuffixesKey = fileSuffixesKey;
    this.defaultFileSuffixes = defaultSuffixes;

  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(configuration.getStringArray(fileSuffixesKey))
      .filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
    if (suffixes.length > 0) {
      return suffixes;
    }
    return defaultFileSuffixes.split(",");
  }

  @Override
  public boolean publishAllFiles() {
    return false;
  }
}
