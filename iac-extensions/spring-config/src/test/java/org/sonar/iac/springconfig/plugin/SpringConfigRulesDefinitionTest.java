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
package org.sonar.iac.springconfig.plugin;

import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

class SpringConfigRulesDefinitionTest {

  @Test
  void shouldLoadRulesFromRepository() {
    var repository = javaconfigRuleRepository(9, 3);
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("Sonar");
    assertThat(repository.language()).isEqualTo("java");
    assertThat(repository.key()).isEqualTo("javaconfig");
    assertThat(repository.rules()).isEmpty();
  }

  private static RulesDefinition.Repository javaconfigRuleRepository(int major, int minor) {
    SonarRuntime sonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(major, minor), SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
    var rulesDefinition = new SpringConfigRulesDefinition(sonarRuntime);
    var context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    return context.repository(SpringConfigExtension.REPOSITORY_KEY);
  }

}
