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
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.iac.common.extension.IacDefaultProfileDefinition;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.InstanceOfAssertFactories.list;

public abstract class AbstractProfileDefinitionTest {
  protected abstract IacDefaultProfileDefinition getProfileDefinition();

  protected abstract String languageKey();

  protected int minimalRulesCount() {
    return 2;
  }

  protected Consumer<List<? extends BuiltInQualityProfilesDefinition.BuiltInActiveRule>> additionalRulesAssert() {
    return rules -> {
      // no-op by default
    };
  }

  @Test
  void shouldCreateAnsibleProfileDefinition() {
    var definition = getProfileDefinition();
    var context = new BuiltInQualityProfilesDefinition.Context();

    definition.define(context);
    var profile = context.profile(languageKey(), "Sonar way");

    assertThat(profile)
      .returns(languageKey(), from(BuiltInQualityProfilesDefinition.BuiltInQualityProfile::language))
      .returns("Sonar way", from(BuiltInQualityProfilesDefinition.BuiltInQualityProfile::name))
      .extracting(BuiltInQualityProfilesDefinition.BuiltInQualityProfile::rules, as(list(BuiltInQualityProfilesDefinition.BuiltInActiveRule.class)))
      .hasSizeGreaterThanOrEqualTo(minimalRulesCount())
      .satisfies(additionalRulesAssert());
  }
}
