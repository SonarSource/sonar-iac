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
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class ArmLanguage extends AbstractLanguage {

  public static final String KEY = "azureresourcemanager";
  public static final String NAME = "Azure Resource Manager";

  private final Configuration configuration;

  public ArmLanguage(Configuration configuration) {
    super(KEY, NAME);
    this.configuration = configuration;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(configuration.getStringArray(ArmSettings.FILE_SUFFIXES_KEY))
      .filter(s -> !s.isBlank()).toArray(String[]::new);
    if (suffixes.length > 0) {
      return suffixes;
    }
    return ArmSettings.FILE_SUFFIXES_DEFAULT_VALUE.split(",");
  }
}
