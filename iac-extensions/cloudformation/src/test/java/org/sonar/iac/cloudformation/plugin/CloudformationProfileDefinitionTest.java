/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationProfileDefinitionTest {

  @Test
  void should_create_sonar_way_profile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    CloudformationProfileDefinition definition = new CloudformationProfileDefinition();
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("cloudformation", "Sonar way");
    assertThat(profile.language()).isEqualTo("cloudformation");
    assertThat(profile.name()).isEqualTo("Sonar way");
    assertThat(profile.rules().size()).isEqualTo(2);
  }
}
