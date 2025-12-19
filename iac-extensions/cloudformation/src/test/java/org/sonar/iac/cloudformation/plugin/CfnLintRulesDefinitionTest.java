/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.plugin;

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
import org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.api.rules.CleanCodeAttribute.CONVENTIONAL;
import static org.sonar.api.rules.CleanCodeAttribute.LOGICAL;
import static org.sonar.api.rules.RuleType.BUG;
import static org.sonar.api.rules.RuleType.CODE_SMELL;
import static org.sonar.api.rules.RuleType.SECURITY_HOTSPOT;
import static org.sonar.api.rules.RuleType.VULNERABILITY;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertNoRepositoryIsDefined;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;

class CfnLintRulesDefinitionTest {

  @MethodSource(value = "org.sonar.iac.common.testing" +
    ".AbstractExternalRulesDefinitionAssertions#externalRepositoryShouldBeInitializedWithSonarRuntime")
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
        263,
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

  @Test
  void externalRulesShouldCarryTheCorrectAttributeAndImpact() {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new CfnLintRulesDefinition(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION);
    rulesDefinition.define(context);
    var repository = context.repository("external_cfn-lint");

    for (String ruleKey : rulesDefinition.getRuleLoader().ruleKeys()) {
      var rule = repository.rule(ruleKey);
      var ruleType = rule.type();
      CleanCodeAttribute cleanCodeAttribute = rule.cleanCodeAttribute();
      Map<SoftwareQuality, Severity> impacts = rule.defaultImpacts();
      if (ruleType == CODE_SMELL) {
        assertThat(cleanCodeAttribute).isEqualTo(CONVENTIONAL);
        if (org.sonar.api.rule.Severity.MAJOR.equals(rule.severity())) {
          assertThat(impacts).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.MEDIUM));
        } else {
          assertThat(impacts).containsOnly(Map.entry(SoftwareQuality.MAINTAINABILITY, Severity.LOW));
        }
      } else if (ruleType == BUG) {
        assertThat(cleanCodeAttribute).isEqualTo(LOGICAL);
        assertThat(impacts).containsOnly(Map.entry(SoftwareQuality.RELIABILITY, Severity.MEDIUM));
      } else if (ruleType == VULNERABILITY || ruleType == SECURITY_HOTSPOT) {
        throw new IllegalStateException("No rule should have type VULNERABILITY or SECURITY_HOTSPOT");
      }
    }
  }
}
