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
package org.sonar.iac.terraform.checks.azure;

import org.sonar.iac.terraform.checks.ResourceVisitor;

import static org.sonar.iac.terraform.checks.UnencryptedCloudServicesCheck.MESSAGE_MAKE_SURE;

public class AzureUnencryptedCloudServicesCheckPart extends ResourceVisitor {
  @Override
  protected void registerResourceConsumer() {
    register("azurerm_data_lake_store",
      resource -> resource.attribute("encryption_state")
        .reportIfValueMatches("DISABLED", MESSAGE_MAKE_SURE));
  }
}
