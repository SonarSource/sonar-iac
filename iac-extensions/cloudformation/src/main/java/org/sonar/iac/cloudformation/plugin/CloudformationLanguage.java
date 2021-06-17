/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class CloudformationLanguage extends AbstractLanguage {

  public static final String KEY = "cloudformation";
  public static final String NAME = "Cloudformation";

  private final Configuration configuration;

  public CloudformationLanguage(Configuration configuration) {
    super(KEY, NAME);
    this.configuration = configuration;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(configuration.getStringArray(CloudformationSettings.FILE_SUFFIXES_KEY))
      .filter(s -> !s.trim().isEmpty()).toArray(String[]::new);
    return suffixes.length > 0 ? suffixes : CloudformationSettings.FILE_SUFFIXES_DEFAULT_VALUE.split(",");
  }
}
