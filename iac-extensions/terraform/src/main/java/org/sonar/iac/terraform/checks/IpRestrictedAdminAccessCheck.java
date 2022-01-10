/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2021 SonarSource SA
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
import org.sonar.iac.terraform.checks.aws.AwsIpRestrictedAdminAccessCheckPart;
import org.sonar.iac.terraform.checks.azure.AzureIpRestrictedAdminAccessCheckPart;

@Rule(key = "S6321")
public class IpRestrictedAdminAccessCheck implements IacCheck {

  public static final String MESSAGE = "Restrict IP addresses authorized to access administration services.";
  public static final String SECONDARY_MSG = "Related protocol setting.";
  public static final String ALL_IPV4 = "0.0.0.0/0";
  public static final String ALL_IPV6 = "::/0";
  public static final int SSH_PORT = 22;
  public static final int RDP_PORT = 3389;

  @Override
  public void initialize(InitContext init) {
    new AwsIpRestrictedAdminAccessCheckPart().initialize(init);
    new AzureIpRestrictedAdminAccessCheckPart().initialize(init);
  }

  public static boolean rangeContainsSshOrRdpPort(int from, int to) {
    return (SSH_PORT >= from && SSH_PORT <= to) || (RDP_PORT >= from && RDP_PORT <= to);
  }
}
