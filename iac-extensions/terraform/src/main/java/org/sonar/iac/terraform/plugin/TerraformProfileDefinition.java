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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public class TerraformProfileDefinition implements BuiltInQualityProfilesDefinition {

  static final String PROFILE_NAME = "Sonar way";
  static final String SONAR_WAY_PATH = "org/sonar/l10n/terraform/rules/terraform/Sonar_way_profile.json";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(PROFILE_NAME, TerraformLanguage.KEY);
    BuiltInQualityProfileJsonLoader.load(profile, TerraformExtension.REPOSITORY_KEY, SONAR_WAY_PATH);
    profile.setDefault(true);
    profile.done();
  }
}
