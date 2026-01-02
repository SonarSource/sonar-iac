/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.checks.aws.AwsIpRestrictedAdminAccessCheckPart;
import org.sonar.iac.terraform.checks.azure.AzureIpRestrictedAdminAccessCheckPart;
import org.sonar.iac.terraform.checks.gcp.GcpIpRestrictedAdminAccessCheckPart;

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck implements IacCheck {

  public static final String SECONDARY_MSG = "Related protocol setting.";

  @Override
  public void initialize(InitContext init) {
    new AwsIpRestrictedAdminAccessCheckPart().initialize(init);
    new AzureIpRestrictedAdminAccessCheckPart().initialize(init);
    new GcpIpRestrictedAdminAccessCheckPart().initialize(init);
  }
}
