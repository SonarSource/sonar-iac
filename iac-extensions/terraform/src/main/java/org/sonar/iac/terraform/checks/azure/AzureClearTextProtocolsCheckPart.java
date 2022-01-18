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

import java.util.List;

import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_OMITTING;

public class AzureClearTextProtocolsCheckPart extends ResourceVisitor {

  @Override
  protected void registerResourceConsumer() {
    register(List.of("azurerm_spring_cloud_app",
                     "azurerm_function_app",
                     "azurerm_function_app_slot",
                     "azurerm_app_service"),
      resource -> resource.attribute("https_only")
        .reportIfAbsence(MESSAGE_OMITTING)
        .reportIfFalse(MESSAGE_CLEAR_TEXT));

    register("azurerm_app_service",
      resource -> resource.block("site_config")
        .ifPresent(block -> block.attribute("ftps_state")
          .reportIfValueMatches("AllAllowed", MESSAGE_CLEAR_TEXT)));

    register("azurerm_cdn_endpoint",
      resource -> resource.attribute("is_http_allowed")
        .reportIfAbsence(MESSAGE_OMITTING)
        .reportIfTrue(MESSAGE_CLEAR_TEXT));

    register("azurerm_redis_enterprise_database",
      resource -> resource.attribute("client_protocol")
        .reportIfValueMatches("PLAINTEXT", MESSAGE_CLEAR_TEXT));
  }
}
