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
package org.sonar.iac.common.yaml;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class YamlEmptyBuiltInProfileDefinitionTest {

  @Test
  void shouldCreateSonarWayProfile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    YamlEmptyBuiltInProfileDefinition definition = new YamlEmptyBuiltInProfileDefinition();
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("yaml", "Sonar way");
    assertThat(profile.language()).isEqualTo("yaml");
    assertThat(profile.name()).isEqualTo("Sonar way");
    assertThat(profile.rules()).isEmpty();
  }
}
