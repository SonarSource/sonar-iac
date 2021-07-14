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
    CloudformationLanguage language = new CloudformationLanguage();
    assertThat(language.getFileSuffixes()).isEmpty();
  }
}
