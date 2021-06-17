/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class TerraformLanguage extends AbstractLanguage {

  static final String KEY = "terraform";
  static final String NAME = "Terraform";

  private final Configuration configuration;

  public TerraformLanguage(Configuration configuration) {
    super(KEY, NAME);
    this.configuration = configuration;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(configuration.getStringArray(TerraformSettings.FILE_SUFFIXES_KEY))
      .filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
    return suffixes.length > 0 ? suffixes : TerraformSettings.FILE_SUFFIXES_DEFAULT_VALUE.split(",");
  }
}
