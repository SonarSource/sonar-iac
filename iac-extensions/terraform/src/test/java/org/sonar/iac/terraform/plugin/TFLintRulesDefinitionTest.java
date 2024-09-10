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
package org.sonar.iac.terraform.plugin;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.issue.impact.Severity;
import org.sonar.api.issue.impact.SoftwareQuality;
import org.sonar.api.rules.CleanCodeAttribute;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.rules.CleanCodeAttribute.CONVENTIONAL;
import static org.sonar.api.rules.CleanCodeAttribute.LOGICAL;
import static org.sonar.api.rules.RuleType.BUG;
import static org.sonar.api.rules.RuleType.CODE_SMELL;
import static org.sonar.api.rules.RuleType.SECURITY_HOTSPOT;
import static org.sonar.api.rules.RuleType.VULNERABILITY;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertExistingRepository;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertNoRepositoryIsDefined;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class TFLintRulesDefinitionTest {

  @MethodSource(value = "org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions#externalRepositoryShouldBeInitializedWithSonarRuntime")
  @ParameterizedTest
  void externalRepositoryShouldBeInitializedWithSonarRuntime(SonarRuntime sonarRuntime, boolean shouldSupportCCT) {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new TFLintRulesDefinition(sonarRuntime);

    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      rulesDefinition.define(context);
      assertExistingRepository(
        context,
        rulesDefinition,
        "tflint",
        "TFLINT",
        "terraform",
        1712,
        shouldSupportCCT);
    } else {
      assertNoRepositoryIsDefined(context, rulesDefinition);
    }
  }

  @Test
  void noOpRulesDefinitionShouldNotDefineAnyRule() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition noOpRulesDefinition = TFLintRulesDefinition.noOpInstanceForSL(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    noOpRulesDefinition.define(context);
    assertNoRepositoryIsDefined(context, noOpRulesDefinition);
  }

  @Test
  void externalRulesShouldCarryTheCorrectAttributeAndImpact() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new TFLintRulesDefinition(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    rulesDefinition.define(context);
    var repository = context.repository("external_tflint");

    for (String ruleKey : rulesDefinition.getRuleLoader().ruleKeys()) {
      var rule = repository.rule(ruleKey);
      var ruleType = rule.type();
      CleanCodeAttribute cleanCodeAttribute = rule.cleanCodeAttribute();
      Map<SoftwareQuality, Severity> impacts = rule.defaultImpacts();
      if (ruleType == CODE_SMELL) {
        assertThat(cleanCodeAttribute).isEqualTo(CONVENTIONAL);
        assertThat(impacts).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
      } else if (ruleType == BUG) {
        assertThat(cleanCodeAttribute).isEqualTo(LOGICAL);
        assertThat(impacts).containsOnly(Map.entry(SoftwareQuality.RELIABILITY, Severity.MEDIUM));
      } else if (ruleType == SECURITY_HOTSPOT) {
        // Security Hotspots do not have a clean code attribute or impacts
        assertThat(cleanCodeAttribute).isNull();
        assertThat(impacts).isEmpty();
      } else if (ruleType == VULNERABILITY) {
        throw new IllegalStateException("No rule should have type VULNERABILITY");
      }
    }
  }
}
