/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import java.util.function.Consumer;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.iac.common.testing.AbstractExtensionTest;

import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class TerraformExtensionTest extends AbstractExtensionTest {

  @Override
  protected SonarRuntime sonarQubeRuntime() {
    return SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;
  }

  @Override
  protected SonarRuntime sonarLintRuntime() {
    return SONARLINT_RUNTIME_9_9;
  }

  @Override
  protected Consumer<Plugin.Context> extensionDefiner() {
    return TerraformExtension::define;
  }

  @Override
  protected int extensionsCountOnSQ() {
    return 11;
  }

  @Override
  protected int extensionsCountOnSL() {
    return 10;
  }
}
