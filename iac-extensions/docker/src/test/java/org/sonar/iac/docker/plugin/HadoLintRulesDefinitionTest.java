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
package org.sonar.iac.docker.plugin;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class HadoLintRulesDefinitionTest {

  @Test
  void rulesHaveValidSeverityAndType() {
    ExternalRuleLoader ruleLoader = HadoLintRulesDefinition.RULE_LOADER;
    Set<String> ruleKeys = ruleLoader.ruleKeys();

    assertDoesNotThrow(() -> {
      for (String ruleKey : ruleKeys) {
        ruleLoader.ruleSeverity(ruleKey);
        ruleLoader.ruleType(ruleKey);
      }
    });
  }

  @Test
  void createExternalHadoLintRepository() {
    RulesDefinition.Context context = new RulesDefinition.Context();
    HadoLintRulesDefinition hadoLintRulesDefinition = new HadoLintRulesDefinition();
    hadoLintRulesDefinition.define(context);

    assertThat(context.repositories()).hasSize(1);
    RulesDefinition.Repository repository = context.repository("external_hado-lint");
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo("Haskell Dockerfile Linter");
    assertThat(repository.language()).isEqualTo("docker");
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(99);
  }
}
