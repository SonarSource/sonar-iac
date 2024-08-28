/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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

import org.junit.jupiter.api.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_RUNTIME_10_6;

class TerraformExtensionTest {

  @Test
  void shouldDefineExtensionsOnSonarqube() {
    Plugin.Context context = new Plugin.Context(SONAR_RUNTIME_10_6);
    TerraformExtension.define(context);
    assertThat(context.getExtensions()).hasSize(11);
  }

  @Test
  void shouldDefineExtensionsOnSonarlint() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.create(3, 14));
    Plugin.Context context = new Plugin.Context(runtime);
    TerraformExtension.define(context);
    assertThat(context.getExtensions()).hasSize(10);
  }
}
