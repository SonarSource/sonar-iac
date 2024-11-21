/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
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
package org.sonar.iac.terraform.plugin;

import org.sonar.api.SonarRuntime;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

@ScannerSide
public class TFLintRulesDefinition extends AbstractExternalRulesDefinition {

  public static final String LINTER_KEY = "tflint";
  public static final String LINTER_NAME = "TFLINT";

  public TFLintRulesDefinition(SonarRuntime sonarRuntime) {
    super(sonarRuntime, LINTER_NAME);
  }

  public static TFLintRulesDefinition noOpInstanceForSL(SonarRuntime sonarRuntime) {
    return new TFLintRulesDefinition(sonarRuntime) {
      @Override
      public void define(Context context) {
        // nothing to do here
      }

      @Override
      public ExternalRuleLoader getRuleLoader() {
        return null;
      }
    };
  }

  @Override
  public String languageKey() {
    return TerraformLanguage.KEY;
  }

  @Override
  public String repositoryKey() {
    return LINTER_KEY;
  }
}
