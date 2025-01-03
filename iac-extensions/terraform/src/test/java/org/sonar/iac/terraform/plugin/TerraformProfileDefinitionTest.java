/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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

import java.util.List;
import java.util.function.Consumer;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.iac.common.extension.IacDefaultProfileDefinition;
import org.sonar.iac.common.testing.AbstractProfileDefinitionTest;

import static org.assertj.core.api.Assertions.assertThat;

class TerraformProfileDefinitionTest extends AbstractProfileDefinitionTest {

  @Override
  protected IacDefaultProfileDefinition getProfileDefinition() {
    return new TerraformProfileDefinition();
  }

  @Override
  protected String languageKey() {
    return "terraform";
  }

  @Override
  protected int minimalRulesCount() {
    return 3;
  }

  @Override
  protected Consumer<List<? extends BuiltInQualityProfilesDefinition.BuiltInActiveRule>> additionalRulesAssert() {
    return rules -> assertThat(rules)
      .extracting(BuiltInQualityProfilesDefinition.BuiltInActiveRule::ruleKey)
      .doesNotContain("S6245") // DisabledS3EncryptionCheck - deprecated
      .doesNotContain("S2260"); // ParsingErrorCheck
  }
}
