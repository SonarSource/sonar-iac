/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
package org.sonar.iac.kubernetes.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
class KubernetesProfileDefinitionTest {

  @Test
  void should_create_sonar_way_profile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    KubernetesProfileDefinition definition = new KubernetesProfileDefinition();
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("kubernetes", "Sonar way");
    assertThat(profile.language()).isEqualTo("kubernetes");
    assertThat(profile.name()).isEqualTo("Sonar way");
    assertThat(profile.rules()).hasSizeGreaterThan(0);
    assertThat(profile.rules()).extracting(BuiltInQualityProfilesDefinition.BuiltInActiveRule::ruleKey)
      .contains("S6428") // ContainerPrivilegedModeCheck
      .doesNotContain("S2260"); // ParsingErrorCheck
  }

}
