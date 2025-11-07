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
package org.sonar.iac.docker.plugin;

import org.sonar.api.SonarRuntime;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.iac.common.reports.AbstractExternalRulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

@ScannerSide
public class HadolintRulesDefinition extends AbstractExternalRulesDefinition {
  public static final String LINTER_KEY = "hadolint";
  public static final String LINTER_NAME = "HADOLINT";

  public HadolintRulesDefinition(SonarRuntime sonarRuntime) {
    super(sonarRuntime, LINTER_NAME);
  }

  public static HadolintRulesDefinition noOpInstanceForSL(SonarRuntime sonarRuntime) {
    return new HadolintRulesDefinition(sonarRuntime) {
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
    return DockerLanguage.KEY;
  }

  @Override
  public String repositoryKey() {
    return LINTER_KEY;
  }
}
