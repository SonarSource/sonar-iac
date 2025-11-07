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
package org.sonar.iac.common.reports;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;

import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertExistingRepository;
import static org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions.assertNoRepositoryIsDefined;

class AbstractExternalRulesDefinitionTest {

  @MethodSource(value = "org.sonar.iac.common.testing.AbstractExternalRulesDefinitionAssertions#externalRepositoryShouldBeInitializedWithSonarRuntime")
  @ParameterizedTest
  void externalRepositoryShouldBeInitializedWithSonarRuntime(SonarRuntime sonarRuntime, boolean shouldSupportCCT) {
    var context = new RulesDefinition.Context();
    AbstractExternalRulesDefinition rulesDefinition = new TestExternalRulesDefinition(sonarRuntime);

    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      rulesDefinition.define(context);
      assertExistingRepository(
        context,
        rulesDefinition,
        "testKey",
        "testName",
        "testLanguage",
        1,
        shouldSupportCCT);
    } else {
      assertNoRepositoryIsDefined(context, rulesDefinition);
    }
  }
}

class TestExternalRulesDefinition extends AbstractExternalRulesDefinition {

  protected TestExternalRulesDefinition(SonarRuntime sonarRuntime) {
    super(sonarRuntime, "testName");
  }

  @Override
  public String languageKey() {
    return "testLanguage";
  }

  @Override
  public String repositoryKey() {
    return "testKey";
  }
}
