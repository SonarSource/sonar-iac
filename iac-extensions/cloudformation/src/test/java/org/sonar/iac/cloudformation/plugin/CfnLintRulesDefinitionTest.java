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
package org.sonar.iac.cloudformation.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;
import org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions;

import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertNoRepositoryIsDefined;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class CfnLintRulesDefinitionTest {

  @MethodSource(value = "org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions#externalRepositoryShouldBeInitializedWithSonarRuntime")
  @ParameterizedTest
  void externalRepositoryShouldBeInitializedWithSonarRuntime(SonarRuntime sonarRuntime, boolean shouldSupportCCT) {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new CfnLintRulesDefinition(sonarRuntime);

    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      rulesDefinition.define(context);
      AbstractExternalRulesDefinitionAssertions.assertExistingRepository(
        context,
        rulesDefinition,
        "cfn-lint",
        "CFN-LINT",
        "cloudformation",
        228,
        shouldSupportCCT);
    } else {
      assertNoRepositoryIsDefined(context, rulesDefinition);
    }
  }

  @Test
  void noOpRulesDefinitionShouldNotDefineAnyRule() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition noOpRulesDefinition = CfnLintRulesDefinition.noOpInstanceForSL(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    noOpRulesDefinition.define(context);
    assertNoRepositoryIsDefined(context, noOpRulesDefinition);
  }
}
