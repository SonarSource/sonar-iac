/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformProfileDefinitionTest {

  @Test
  void should_create_sonar_way_profile() {
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    TerraformProfileDefinition definition = new TerraformProfileDefinition();
    definition.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("terraform", "Sonar way");
    assertThat(profile.language()).isEqualTo("terraform");
    assertThat(profile.name()).isEqualTo("Sonar way");
    assertThat(profile.rules().size()).isGreaterThan(2);
    assertThat(profile.rules()).extracting(BuiltInQualityProfilesDefinition.BuiltInActiveRule::ruleKey)
      .contains("S6245") // DisabledS3EncryptionCheck
      .doesNotContain("S2260"); // ParsingErrorCheck
  }
}
