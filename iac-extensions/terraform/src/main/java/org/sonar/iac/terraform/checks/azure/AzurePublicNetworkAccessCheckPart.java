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
package org.sonar.iac.terraform.checks.azure;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.reportOnTrue;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.reportResource;

public class AzurePublicNetworkAccessCheckPart {

  private static final String OMITTED_MESSAGE = "Omitting %s allows network access from the Internet. Make sure it is safe here.";
  private static final String NETWORK_ACCESS_MESSAGE = "Make sure allowing public network access is safe here.";
  private static final String PUBLIC_NETWORK_ACCESS_ENABLED = "public_network_access_enabled";

  public static void checkPublicNetworkAccess(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, PUBLIC_NETWORK_ACCESS_ENABLED, AttributeTree.class)
      .ifPresentOrElse(
        enabled -> reportOnTrue(ctx, enabled, NETWORK_ACCESS_MESSAGE),
        () -> reportResource(ctx, resource, String.format(OMITTED_MESSAGE, PUBLIC_NETWORK_ACCESS_ENABLED)));
  }

}
