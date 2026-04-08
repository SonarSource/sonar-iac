/*
 * SonarQube IaC Plugin
 * Copyright (C) SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * You can redistribute and/or modify this program under the terms of
 * the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.resources.AbstractLanguage;
import org.sonar.iac.common.languages.IacLanguage;

public class CloudformationLanguage extends AbstractLanguage {

  public static final String KEY = IacLanguage.CLOUDFORMATION.getKey();
  public static final String NAME = IacLanguage.CLOUDFORMATION.getName();

  public CloudformationLanguage() {
    super(KEY, NAME);
  }

  @Override
  public String[] getFileSuffixes() {
    return new String[0];
  }
}
