/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
    if (!TerraformExtension.LANGUAGE_KEY.equals(inputFile.language())) {
      return true;
    }
    String[] excludedPatterns = this.configuration.getStringArray(TerraformSettings.EXCLUSIONS_KEY);
    String relativePath = inputFile.uri().toString();
    return !WildcardPattern.match(WildcardPattern.create(excludedPatterns), relativePath);
  }
}
