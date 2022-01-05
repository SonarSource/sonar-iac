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
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6329")
public class AssignedPublicIPAddressCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using public IP address is safe here.";
  private static final String SECONDARY_INSTANCE_MESSAGE = "Related instance";
  private static final String SECONDARY_TEMPLATE_MESSAGE = "Related template";

  @Override
  protected void registerResourceChecks() {
    register(AssignedPublicIPAddressCheck::checkDMSReplicationInstance, "aws_dms_replication_instance");
    register(AssignedPublicIPAddressCheck::checkEC2Instance, "aws_instance");
    register(AssignedPublicIPAddressCheck::checkEC2LaunchTemplate, "aws_launch_template");
  }

  private static void checkDMSReplicationInstance(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "publicly_accessible", AttributeTree.class)
      .ifPresentOrElse(publiclyAccessible -> reportOnTrue(ctx, publiclyAccessible, MESSAGE,
          new SecondaryLocation(resource.labels().get(0),SECONDARY_INSTANCE_MESSAGE)),
        () -> reportResource(ctx, resource, MESSAGE));
  }

  private static void checkEC2Instance(CheckContext ctx, BlockTree resource) {

    PropertyUtils.get(resource, "associate_public_ip_address", AttributeTree.class)
      .ifPresentOrElse(publiclyAccessible -> reportOnTrue(ctx, publiclyAccessible, MESSAGE,
          new SecondaryLocation(resource.labels().get(0), SECONDARY_INSTANCE_MESSAGE)),
        () -> reportResource(ctx, resource, MESSAGE));
  }

  private static void checkEC2LaunchTemplate(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "network_interfaces", BlockTree.class)
      .ifPresentOrElse(networkInterfaces -> checkNetworkInterfaces(ctx, networkInterfaces, resource),
        () -> reportResource(ctx, resource, MESSAGE));
  }

  private static void checkNetworkInterfaces(CheckContext ctx, BlockTree networkInterfaces, BlockTree resource) {
    SecondaryLocation resourceAsSecondary = new SecondaryLocation(resource.labels().get(0), SECONDARY_TEMPLATE_MESSAGE);
    PropertyUtils.get(networkInterfaces, "associate_public_ip_address", AttributeTree.class)
      .ifPresentOrElse(associatePublicIpAddress -> reportOnTrue(ctx, associatePublicIpAddress, MESSAGE, resourceAsSecondary),
        () -> ctx.reportIssue(networkInterfaces.key(), MESSAGE, resourceAsSecondary));
  }
}
