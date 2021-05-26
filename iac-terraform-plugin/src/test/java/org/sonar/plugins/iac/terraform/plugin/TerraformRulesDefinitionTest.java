/*
 * SonarQube IaC Terraform Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
package org.sonar.plugins.iac.terraform.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.iac.terraform.checks.TerraformCheckList;

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
