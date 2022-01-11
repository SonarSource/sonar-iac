/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
