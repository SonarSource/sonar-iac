/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.iac;

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class IacPluginTest {

  private static final Version VERSION_8_9 = Version.create(8, 9);

  private final IacPlugin iacPlugin = new IacPlugin();

  @Test
  void sonarqube_extensions() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    iacPlugin.define(context);
    assertThat(context.getExtensions()).hasSize(19);
  }
}

