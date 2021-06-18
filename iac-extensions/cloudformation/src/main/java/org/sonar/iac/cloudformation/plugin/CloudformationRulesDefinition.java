/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.plugin;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.iac.cloudformation.checks.CloudformationCheckList;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public class CloudformationRulesDefinition implements RulesDefinition {

  private static final String RESOURCE_FOLDER = "org/sonar/l10n/cloudformation/rules/cloudformation";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(CloudformationExtension.REPOSITORY_KEY, CloudformationLanguage.KEY)
      .setName(CloudformationExtension.REPOSITORY_NAME);
    RuleMetadataLoader metadataLoader = new RuleMetadataLoader(RESOURCE_FOLDER);
    metadataLoader.addRulesByAnnotatedClass(repository, CloudformationCheckList.checks());
    repository.done();
  }
}
