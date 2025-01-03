/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.common.testing;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.sonar.iac.common.testing.IacTestUtils.SONARLINT_RUNTIME_9_9;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION;
import static org.sonar.iac.common.testing.IacTestUtils.SONAR_QUBE_9_9;

public class AbstractExternalRulesDefinitionAssertions {

  public static void assertExistingRepository(
    RulesDefinition.Context context,
    AbstractExternalRulesDefinition rulesDefinition,
    String reportKey,
    String reportName,
    String language,
    int numberOfRules,
    boolean shouldSupportCCT) {

    assertThat(context.repositories()).hasSize(1);
    var repository = context.repository("external_" + reportKey);
    assertThat(repository).isNotNull();
    assertThat(repository.name()).isEqualTo(reportName);
    assertThat(repository.language()).isEqualTo(language);
    assertThat(repository.isExternal()).isTrue();
    assertThat(repository.rules()).hasSize(numberOfRules);

    for (String ruleKey : rulesDefinition.getRuleLoader().ruleKeys()) {
      assertThatNoException().isThrownBy(() -> rulesDefinition.getRuleLoader().ruleSeverity(ruleKey));
      assertThatNoException().isThrownBy(() -> rulesDefinition.getRuleLoader().ruleType(ruleKey));
    }

    assertThat(rulesDefinition.getRuleLoader().isCleanCodeImpactsAndAttributesSupported()).isEqualTo(shouldSupportCCT);
  }

  public static void assertNoRepositoryIsDefined(RulesDefinition.Context context, AbstractExternalRulesDefinition rulesDefinition) {
    assertThat(context.repositories()).isEmpty();
    assertThat(rulesDefinition.getRuleLoader()).isNull();
  }

  public static Stream<Arguments> externalRepositoryShouldBeInitializedWithSonarRuntime() {
    return Stream.of(
      // sonarRuntime, shouldSupportCCT
      Arguments.of(SONAR_QUBE_10_6_CCT_SUPPORT_MINIMAL_VERSION, true),
      Arguments.of(SONAR_QUBE_9_9, false),
      Arguments.of(SONARLINT_RUNTIME_9_9, false));
  }
}
