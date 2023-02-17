/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public abstract class IacDefaultProfileDefinition implements BuiltInQualityProfilesDefinition, ProvideLanguageKey {

  private static final String PROFILE_NAME = "Sonar way";
  private static final String SONAR_WAY_PATH_FORMAT = "org/sonar/l10n/%1$s/rules/%1$s/Sonar_way_profile.json";

  @Override
  public void define(Context context) {
    String languageKey = languageKey();
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(PROFILE_NAME, languageKey);
    String sonarWayPath = String.format(SONAR_WAY_PATH_FORMAT, languageKey);
    BuiltInQualityProfileJsonLoader.load(profile, languageKey, sonarWayPath);
    profile.setDefault(true);
    profile.done();
  }

}
