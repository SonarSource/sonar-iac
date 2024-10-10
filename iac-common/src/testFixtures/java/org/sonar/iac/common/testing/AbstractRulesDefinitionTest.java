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
package org.sonar.iac.common.testing;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.extension.IacRulesDefinition;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

public abstract class AbstractRulesDefinitionTest {

  protected abstract Version sonarVersion();

  protected abstract IacRulesDefinition getRulesDefinition(SonarRuntime sonarRuntime);

  protected abstract String languageKey();

  protected abstract List<Class<?>> checks();

  @Test
  void testActivation() {
    var repository = createRuleRepository();

    assertThat(repository)
      .isNotNull()
      .returns("Sonar", from(RulesDefinition.Repository::name))
      .returns(languageKey(), from(RulesDefinition.Repository::language))
      .extracting(RulesDefinition.ExtendedRepository::rules, as(list(BuiltInQualityProfilesDefinition.BuiltInActiveRule.class)))
      .hasSameSizeAs(checks());
  }

  private RulesDefinition.Repository createRuleRepository() {
    var sonarRuntime = SonarRuntimeImpl.forSonarQube(sonarVersion(), SonarQubeSide.SERVER, SonarEdition.DEVELOPER);
    var rulesDefinition = getRulesDefinition(sonarRuntime);
    var context = new RulesDefinition.Context();
    rulesDefinition.define(context);
    return context.repository(languageKey());
  }
}
