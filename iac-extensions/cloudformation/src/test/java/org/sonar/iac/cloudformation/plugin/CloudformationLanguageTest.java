/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;


import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationLanguageTest {

  @Test
  void should_return_terraform_file_suffixes() {
    MapSettings settings = new MapSettings();
    CloudformationLanguage language = new CloudformationLanguage(settings.asConfig());
    assertThat(language.getFileSuffixes()).containsExactly(".json",".yaml",".yml");

    settings.setProperty(CloudformationSettings.FILE_SUFFIXES_KEY, "");
    assertThat(language.getFileSuffixes()).containsExactly(".json",".yaml",".yml");

    settings.setProperty(CloudformationSettings.FILE_SUFFIXES_KEY, ".bar, .foo");
    assertThat(language.getFileSuffixes()).containsOnly(".bar", ".foo");

    settings.setProperty(CloudformationSettings.FILE_SUFFIXES_KEY, ".foo, , ");
    assertThat(language.getFileSuffixes()).containsOnly(".foo");
  }
}
