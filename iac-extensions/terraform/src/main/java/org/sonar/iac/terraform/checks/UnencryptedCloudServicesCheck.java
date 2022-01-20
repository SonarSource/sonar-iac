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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.IacCheck;

@Rule(key = "S6388")
public class UnencryptedCloudServicesCheck extends ResourceVisitor implements IacCheck
{
  public static final String UNENCRYPTED_MESSAGE = "Make sure using unencrypted cloud storage is safe here.";
  public static final String FORMAT_OMITTING = "Omitting %s enables clear-text storage. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("azurerm_data_lake_store",
      resource -> resource.attribute("encryption_state")
        .reportIfValueMatches("Disabled", UNENCRYPTED_MESSAGE));

    register("azurerm_managed_disk",
      resource -> resource.attribute("disk_encryption_set_id")
        .reportIfAbsence(FORMAT_OMITTING));

    register("azurerm_mysql_server",
      resource -> resource.attribute("infrastructure_encryption_enabled")
        .reportIfAbsence(FORMAT_OMITTING)
        .reportIfFalse(UNENCRYPTED_MESSAGE));
  }
}
