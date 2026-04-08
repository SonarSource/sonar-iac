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
package org.sonar.iac.terraform.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformLanguageTest {

  @Test
  void should_return_terraform_file_suffixes() {
    MapSettings settings = new MapSettings();
    TerraformLanguage language = new TerraformLanguage(settings.asConfig());
    assertThat(language.getFileSuffixes()).containsOnly(".tf");

    settings.setProperty(TerraformSettings.FILE_SUFFIXES_KEY, "");
    assertThat(language.getFileSuffixes()).containsOnly(".tf");

    settings.setProperty(TerraformSettings.FILE_SUFFIXES_KEY, ".bar, .foo");
    assertThat(language.getFileSuffixes()).containsOnly(".bar", ".foo");

    settings.setProperty(TerraformSettings.FILE_SUFFIXES_KEY, ".foo, , ");
    assertThat(language.getFileSuffixes()).containsOnly(".foo");
  }
}
