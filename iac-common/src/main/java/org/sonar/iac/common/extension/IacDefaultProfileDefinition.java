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
package org.sonar.iac.common.extension;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public abstract class IacDefaultProfileDefinition implements BuiltInQualityProfilesDefinition, UsesRulesFolder {

  private static final String PROFILE_NAME = "Sonar way";

  @Override
  public void define(Context context) {
    String languageKey = languageKey();
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(PROFILE_NAME, languageKey);
    BuiltInQualityProfileJsonLoader.load(profile, languageKey, sonarWayPath());
    profile.setDefault(true);
    profile.done();
  }
}
