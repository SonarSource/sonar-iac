/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.ExternalRuleLoader;

public class CfnLintRulesDefinition implements RulesDefinition {
  public static final String LINTER_KEY = "cfn-lint";
  public static final String LINTER_NAME = "AWS CloudFormation Linter";
  private static final String RULES_JSON = "org/sonar/l10n/cloudformation/rules/cfn-lint/rules.json";

  public static final ExternalRuleLoader RULE_LOADER = new ExternalRuleLoader(LINTER_KEY, LINTER_NAME, RULES_JSON, CloudformationLanguage.KEY);

  @Override
  public void define(Context context) {
    RULE_LOADER.createExternalRuleRepository(context);
  }
}
