/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import org.sonar.iac.terraform.checks.TerraformCheckList;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformRulesDefinitionTest {

  @Test
  void testActivationSonarLint() {
    RulesDefinition.Repository repository = terraformRuleRepository(9, 3);
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("Sonar");
    assertThat(repository.language()).isEqualTo("terraform");
    assertThat(repository.rules()).hasSize(TerraformCheckList.checks().size());
  }

  private static RulesDefinition.Repository terraformRuleRepository(int major, int minor) {
    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(major, minor), SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
    TerraformRulesDefinition rulesDefinition = new TerraformRulesDefinition(sonarRuntime);
    RulesDefinition.Context context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    return context.repository("terraform");
  }
}
