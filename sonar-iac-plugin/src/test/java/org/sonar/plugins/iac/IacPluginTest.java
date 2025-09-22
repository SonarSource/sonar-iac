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
  void sonarqubeServerExtensionsShouldBeDefinedForCommunityEdition() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SERVER, SonarEdition.COMMUNITY);
    Plugin.Context context = new Plugin.Context(runtime);
    iacPlugin.define(context);
    assertThat(context.getExtensions()).hasSize(55);
  }

  @Test
  void sonarqubeServerExtensionsShouldBeDefinedForDeveloperEdition() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
    Plugin.Context context = new Plugin.Context(runtime);
    iacPlugin.define(context);
    assertThat(context.getExtensions()).hasSize(53);
  }

  @Test
  void sonarqubeCloudExtensionsShouldBeDefined() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(VERSION_8_9, SonarQubeSide.SERVER, SonarEdition.SONARCLOUD);
    Plugin.Context context = new Plugin.Context(runtime);
    iacPlugin.define(context);
    assertThat(context.getExtensions()).hasSize(53);
  }

  @Test
  void sonarQubeIDEExtensionsShouldBeDefined() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(VERSION_8_9);
    Plugin.Context context = new Plugin.Context(runtime);
    iacPlugin.define(context);
    // Doesn't contain 3 external report properties (tf, cf, docker) but k8s sonarlintfilelistener
    assertThat(context.getExtensions()).hasSize(50);
  }
}
