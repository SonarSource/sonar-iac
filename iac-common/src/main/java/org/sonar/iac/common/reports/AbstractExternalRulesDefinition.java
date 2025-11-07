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

import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.common.extension.UsesRulesFolder;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

public abstract class AbstractExternalRulesDefinition implements RulesDefinition, UsesRulesFolder {
  private final ExternalRuleLoader ruleLoader;

  protected AbstractExternalRulesDefinition(SonarRuntime sonarRuntime, String reportName) {
    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      this.ruleLoader = new ExternalRuleLoader(
        repositoryKey(),
        reportName,
        externalRulesPath(),
        languageKey(),
        sonarRuntime);
    } else {
      this.ruleLoader = null;
    }
  }

  @Override
  public void define(Context context) {
    ruleLoader.createExternalRuleRepository(context);
  }

  public ExternalRuleLoader getRuleLoader() {
    return ruleLoader;
  }
}
