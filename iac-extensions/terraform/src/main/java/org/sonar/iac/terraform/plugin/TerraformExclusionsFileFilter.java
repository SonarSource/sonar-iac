/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFileFilter;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.WildcardPattern;

public class TerraformExclusionsFileFilter implements InputFileFilter {

  private final Configuration configuration;

  public TerraformExclusionsFileFilter(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public boolean accept(InputFile inputFile) {
    if (!TerraformLanguage.KEY.equals(inputFile.language())) {
      return true;
    }
    String[] excludedPatterns = this.configuration.getStringArray(TerraformSettings.EXCLUSIONS_KEY);
    String relativePath = inputFile.uri().toString();
    return !WildcardPattern.match(WildcardPattern.create(excludedPatterns), relativePath);
  }
}
