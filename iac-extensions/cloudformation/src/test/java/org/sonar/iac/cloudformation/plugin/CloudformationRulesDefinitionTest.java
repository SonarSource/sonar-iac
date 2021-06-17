/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationRulesDefinitionTest {

  @Test
  void testActivationSonarLint() {
    CloudformationRulesDefinition rulesDefinition = new CloudformationRulesDefinition();
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    RulesDefinition.Repository repository = context.repository("cloudformation");
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("SonarQube");
    assertThat(repository.language()).isEqualTo("cloudformation");
    assertThat(repository.rules()).hasSize(CloudformationCheckList.checks().size());
  }
}
