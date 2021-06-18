/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.plugins.iac;

import org.sonar.api.Plugin;
import org.sonar.iac.cloudformation.plugin.CloudformationExtension;
import org.sonar.iac.terraform.plugin.TerraformExtension;

public class IacPlugin implements Plugin {

  @Override
  public void define(Context context) {
    TerraformExtension.define(context);
    CloudformationExtension.define(context);
  }
}
