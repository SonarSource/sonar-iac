/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.plugin;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.terraform.checks.TerraformCheckList;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class TerraformRulesDefinition implements RulesDefinition {

  private static final String RESOURCE_FOLDER = "org/sonar/l10n/terraform/rules/terraform";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(TerraformExtension.REPOSITORY_KEY, TerraformLanguage.KEY)
      .setName(TerraformExtension.REPOSITORY_NAME);
    RuleMetadataLoader metadataLoader = new RuleMetadataLoader(RESOURCE_FOLDER);
    metadataLoader.addRulesByAnnotatedClass(repository, TerraformCheckList.checks());
    repository.done();
  }
}
