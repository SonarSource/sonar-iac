/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.kubernetes.plugin;

import java.util.function.Consumer;
import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.testing.AbstractExtensionTest;

import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;

class KubernetesExtensionTest extends AbstractExtensionTest {

  private static final Version VERSION_9_5 = Version.create(9, 5);

  @Override
  protected SonarRuntime sonarQubeRuntime() {
    return SonarRuntimeImpl.forSonarQube(VERSION_9_5, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
  }

  @Override
  protected SonarRuntime sonarLintRuntime() {
    return SONARLINT_RUNTIME_9_9;
  }

  @Override
  protected Consumer<Plugin.Context> extensionDefiner() {
    return KubernetesExtension::define;
  }

  @Override
  protected int extensionsCountOnSQ() {
    return 7;
  }

  @Override
  protected int extensionsCountOnSL() {
    return 8;
  }
}
