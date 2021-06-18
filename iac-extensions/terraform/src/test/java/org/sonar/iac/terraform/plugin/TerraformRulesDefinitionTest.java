/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.terraform.checks.TerraformCheckList;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformRulesDefinitionTest {

  @Test
  void testActivationSonarLint() {
    TerraformRulesDefinition rulesDefinition = new TerraformRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("terraform");
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("SonarQube");
    assertThat(repository.language()).isEqualTo("terraform");
    assertThat(repository.rules()).hasSize(TerraformCheckList.checks().size());
  }
}
