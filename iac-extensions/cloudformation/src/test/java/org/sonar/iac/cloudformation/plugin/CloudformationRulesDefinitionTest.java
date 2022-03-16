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
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;

import static org.assertj.core.api.Assertions.assertThat;

class CloudformationRulesDefinitionTest {

  @Test
  void testActivationSonarLint() {
    RulesDefinition.Repository repository = cloudformationRuleRepository(9, 3);
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("SonarQube");
    assertThat(repository.language()).isEqualTo("cloudformation");
    assertThat(repository.rules()).hasSize(CloudformationCheckList.checks().size());
  }

  @Test
  void owaspSecurityStandard() {
    RulesDefinition.Repository repository_9_3 = cloudformationRuleRepository(9, 3);
    RulesDefinition.Rule s4423_9_3 = repository_9_3.rule("S4423");
    assertThat(s4423_9_3).isNotNull();
    assertThat(s4423_9_3.securityStandards()).contains("owaspTop10-2021:a2", "owaspTop10-2021:a7");

    RulesDefinition.Repository repository_9_2 = cloudformationRuleRepository(9, 2);
    RulesDefinition.Rule s4423_9_2 = repository_9_2.rule("S4423");
    assertThat(s4423_9_2).isNotNull();
    assertThat(s4423_9_2.securityStandards()).doesNotContain("owaspTop10-2021:a2", "owaspTop10-2021:a7");
  }

  private static RulesDefinition.Repository cloudformationRuleRepository(int major, int minor) {
    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(major, minor), SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
    CloudformationRulesDefinition rulesDefinition = new CloudformationRulesDefinition(sonarRuntime);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    return context.repository("cloudformation");
  }
}
