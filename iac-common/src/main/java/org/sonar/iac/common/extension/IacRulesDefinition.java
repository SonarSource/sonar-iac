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
package org.sonar.iac.common.extension;

import java.util.List;
import org.sonar.api.SonarRuntime;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;

public abstract class IacRulesDefinition implements RulesDefinition, UsesRulesFolder {

  public static final String REPOSITORY_NAME = "SonarAnalyzer";

  private final SonarRuntime runtime;

  protected IacRulesDefinition(SonarRuntime runtime) {
    this.runtime = runtime;
  }

  protected abstract List<Class<?>> checks();

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(languageKey(), languageKey())
      .setName(REPOSITORY_NAME);
    var defaultProfilePath = sonarWayPath();
    var metadataLoader = new RuleMetadataLoader(resourceFolder(), defaultProfilePath, runtime);
    metadataLoader.addRulesByAnnotatedClass(repository, checks());
    repository.done();
  }
}
