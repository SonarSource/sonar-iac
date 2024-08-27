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
package org.sonar.iac.common.reports;

import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

public abstract class AbstractExternalRulesDefinition implements RulesDefinition {
  private static final String RULES_JSON_PATH = "org/sonar/l10n/%s/rules/%s/rules.json";
  private final ExternalRuleLoader ruleLoader;

  protected AbstractExternalRulesDefinition(SonarRuntime sonarRuntime, String reportKey, String reportName, String languageKey) {
    if (sonarRuntime.getProduct() != SonarProduct.SONARLINT) {
      this.ruleLoader = new ExternalRuleLoader(
        reportKey,
        reportName,
        RULES_JSON_PATH.formatted(languageKey, reportKey),
        languageKey,
        sonarRuntime);
    } else {
      this.ruleLoader = null;
    }
  }

  protected AbstractExternalRulesDefinition() {
    this.ruleLoader = null;
  }

  @Override
  public void define(Context context) {
    ruleLoader.createExternalRuleRepository(context);
  }

  public ExternalRuleLoader getRuleLoader() {
    return ruleLoader;
  }
}
