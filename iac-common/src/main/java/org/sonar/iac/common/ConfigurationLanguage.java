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

  public ConfigurationLanguage(String key, String name, Configuration configuration, String defaultSuffixes) {
    super(key, name);
    this.configuration = configuration;
    this.fileSuffixesKey = "sonar." + key + ".file.suffixes";
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
