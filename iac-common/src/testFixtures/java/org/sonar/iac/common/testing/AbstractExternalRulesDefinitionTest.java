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
package org.sonar.iac.common.testing;

import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public abstract class AbstractExternalRulesDefinitionTest {

  protected void assertExistingRepository(
    RulesDefinition.Context context,
    AbstractExternalRulesDefinition rulesDefinition,
    boolean shouldSupportCCT) {
    assertThat(context.repositories()).hasSize(1);
    var repository = context.repository("external_" + reportKey());
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo(reportName());
    assertThat(repository.language()).isEqualTo(language());
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(numberOfRules());

    for (String ruleKey : rulesDefinition.getRuleLoader().ruleKeys()) {
      assertThatNoException().isThrownBy(() -> rulesDefinition.getRuleLoader().ruleSeverity(ruleKey));
      assertThatNoException().isThrownBy(() -> rulesDefinition.getRuleLoader().ruleType(ruleKey));
    }

    assertThat(rulesDefinition.getRuleLoader().isCleanCodeImpactsAndAttributesSupported()).isEqualTo(shouldSupportCCT);
  }

  protected void assertNoRepositoryIsDefined(RulesDefinition.Context context, AbstractExternalRulesDefinition rulesDefinition) {
    assertThat(context.repositories()).isEmpty();
    assertThat(rulesDefinition.getRuleLoader()).isNull();
  }

  protected abstract AbstractExternalRulesDefinition rulesDefinition(SonarRuntime sonarRuntime);

  protected abstract int numberOfRules();

  protected abstract String reportName();

  protected abstract String reportKey();

  protected abstract String language();

  protected abstract AbstractExternalRulesDefinition noOpRulesDefinition();
}
