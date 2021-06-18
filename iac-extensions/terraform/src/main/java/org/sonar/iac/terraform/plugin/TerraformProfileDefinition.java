/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
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
