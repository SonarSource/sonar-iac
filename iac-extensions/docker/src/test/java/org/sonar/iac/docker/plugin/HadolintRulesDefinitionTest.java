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
package org.sonar.iac.docker.plugin;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.rules.CleanCodeAttribute.CONVENTIONAL;
import static org.sonar.api.rules.CleanCodeAttribute.LOGICAL;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertExistingRepository;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertNoRepositoryIsDefined;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class HadolintRulesDefinitionTest {

  @MethodSource(value = "org.sonar.iac.common.testing" +
    ".AbstractExternalRulesDefinitionAssertions#externalRepositoryShouldBeInitializedWithSonarRuntime")
  @ParameterizedTest
  void externalRepositoryShouldBeInitializedWithSonarRuntime(SonarRuntime sonarRuntime, boolean shouldSupportCCT) {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new HadolintRulesDefinition(sonarRuntime);

    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      rulesDefinition.define(context);
      assertExistingRepository(
        context,
        rulesDefinition,
        "hadolint",
        "HADOLINT",
        "docker",
        99,
        shouldSupportCCT);
    } else {
      assertNoRepositoryIsDefined(context, rulesDefinition);
    }
  }

  @Test
  void noOpRulesDefinitionShouldNotDefineAnyRule() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition noOpRulesDefinition = HadolintRulesDefinition.noOpInstanceForSL(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    noOpRulesDefinition.define(context);
    assertNoRepositoryIsDefined(context, noOpRulesDefinition);
  }

  @Test
  void externalRulesShouldCarryTheCorrectAttributeAndImpact() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new HadolintRulesDefinition(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    rulesDefinition.define(context);
    var repository = context.repository("external_hadolint");

    RulesDefinition.Rule codeSmell = repository.rule("DL3001");
    assertThat(codeSmell.name()).isEqualTo("For some bash commands it makes no sense running them in a Docker container like ssh, vim, " +
      "shutdown, service, ps, free, top, kill, mount, ifconfig.");
    assertThat(codeSmell.cleanCodeAttribute()).isEqualTo(CONVENTIONAL);
    assertThat(codeSmell.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.LOW));

    RulesDefinition.Rule bug = repository.rule("DL3000");
    assertThat(bug.name()).isEqualTo("Use absolute WORKDIR.");
    assertThat(bug.cleanCodeAttribute()).isEqualTo(LOGICAL);
    assertThat(bug.defaultImpacts()).containsOnly(Map.entry(SoftwareQuality.RELIABILITY, Severity.HIGH));

    RulesDefinition.Rule hotspot = repository.rule("DL3002");
    assertThat(hotspot.name()).isEqualTo("Last user should not be root.");
    assertThat(hotspot.cleanCodeAttribute()).isNull();
    assertThat(hotspot.defaultImpacts()).isEmpty();
  }
}
