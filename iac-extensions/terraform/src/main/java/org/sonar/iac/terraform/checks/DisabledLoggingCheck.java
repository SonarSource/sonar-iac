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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.checks.aws.AwsDisabledLoggingCheckPart;
import org.sonar.iac.terraform.checks.azure.AzureDisabledLoggingCheckPart;
import org.sonar.iac.terraform.checks.gcp.GcpDisabledLoggingCheckPart;

@Rule(key = "S6258")
public class DisabledLoggingCheck implements IacCheck {

  public static final String MESSAGE = "Make sure that disabling logging is safe here.";
  public static final String MESSAGE_OMITTING = "Omitting %s makes logs incomplete. Make sure it is safe here.";

  @Override
  public void initialize(InitContext init) {
    new AwsDisabledLoggingCheckPart().initialize(init);
    new GcpDisabledLoggingCheckPart().initialize(init);
    new AzureDisabledLoggingCheckPart().initialize(init);
  }
}
