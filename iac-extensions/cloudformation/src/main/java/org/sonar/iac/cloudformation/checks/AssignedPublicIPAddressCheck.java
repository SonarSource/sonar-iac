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
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.TextUtils;

import static org.sonar.iac.common.api.checks.SecondaryLocation.secondary;
import static org.sonar.iac.common.checks.PropertyUtils.get;
import static org.sonar.iac.common.checks.PropertyUtils.value;

@Rule(key = "S6329")
public class AssignedPublicIPAddressCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure allowing public network access is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"%s\" allows network access from the Internet. Make sure it is safe here.";
  private static final String SECONDARY_DMS_MESSAGE = "Related DMS instance";
  private static final String SECONDARY_EC2_INSTANCE_MESSAGE = "Related EC2 instance";
  private static final String SECONDARY_EC2_TEMPLATE_MESSAGE = "Related EC2 template";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::DMS::ReplicationInstance")) {
      checkDMSReplicationInstance(ctx, resource);
    } else if (resource.isType("AWS::EC2::Instance")) {
      checkEC2Instance(ctx, resource);
    } else if (resource.isType("AWS::EC2::LaunchTemplate")) {
      checkEC2LaunchTemplate(ctx, resource);
    }
  }

  private static void checkDMSReplicationInstance(CheckContext ctx, Resource resource) {
    value(resource.properties(), "PubliclyAccessible", ScalarTree.class)
      .ifPresentOrElse(publiclyAccessible -> reportIfTrue(ctx, publiclyAccessible, secondary(resource.type(), SECONDARY_DMS_MESSAGE)),
        () -> reportIfAbsent(ctx, resource, "PubliclyAccessible"));
  }

  private static void checkEC2Instance(CheckContext ctx, Resource resource) {
    get(resource.properties(), "NetworkInterfaces")
      .ifPresentOrElse(networkInterfaces -> checkNetworkInterfaces(ctx, networkInterfaces, secondary(resource.type(), SECONDARY_EC2_INSTANCE_MESSAGE)),
        () -> reportIfAbsent(ctx, resource, "NetworkInterfaces.AssociatePublicIpAddress"));
  }

  private static void checkEC2LaunchTemplate(CheckContext ctx, Resource resource) {
    get(resource.properties(), "LaunchTemplateData")
      .ifPresentOrElse(launchTemplateData -> get(launchTemplateData.value(), "NetworkInterfaces")
        .ifPresentOrElse(networkInterfaces -> checkNetworkInterfaces(ctx, networkInterfaces, secondary(resource.type(), SECONDARY_EC2_TEMPLATE_MESSAGE)),
          () -> reportIfAbsent(ctx, launchTemplateData, "NetworkInterfaces.AssociatePublicIpAddress", resource, SECONDARY_EC2_TEMPLATE_MESSAGE)),
        () -> reportIfAbsent(ctx, resource, "LaunchTemplateData.NetworkInterfaces.AssociatePublicIpAddress"));
  }

  private static void checkNetworkInterfaces(CheckContext ctx, PropertyTree networkInterfaces, SecondaryLocation resourceSecondary) {
    Optional.of(networkInterfaces.value())
      // `NetworkInterfaces` has to be a sequence
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .stream()
      // Find `NetworkInterfaces` element `AssociatePublicIpAddress`
      .flatMap(values -> values.elements().stream())
      .map(element -> value(element, "AssociatePublicIpAddress"))
      .flatMap(Optional::stream)
      .findAny()
      // Report if `AssociatePublicIpAddress` sequence element's value is true, or if it does not exist, or if `NetworkInterfaces` is not a
      // sequence
      .ifPresentOrElse(associatePublicIpAddress -> reportIfTrue(ctx, associatePublicIpAddress, resourceSecondary),
        () -> reportIfAbsent(ctx, networkInterfaces, "AssociatePublicIpAddress", resourceSecondary));
  }

  private static void reportIfAbsent(CheckContext ctx, Resource resource, String propertyName) {
    reportResource(ctx, resource, String.format(OMITTING_MESSAGE, propertyName));
  }

  private static void reportIfAbsent(CheckContext ctx, PropertyTree parent, String propertyName, Resource resource, String secondaryMessage) {
    reportIfAbsent(ctx, parent, propertyName, secondary(resource.type(), secondaryMessage));
  }

  private static void reportIfAbsent(CheckContext ctx, PropertyTree parent, String propertyName, SecondaryLocation secondary) {
    ctx.reportIssue(parent.key(), String.format(OMITTING_MESSAGE, propertyName), secondary);
  }

  private static void reportIfTrue(CheckContext ctx, Tree tree, SecondaryLocation secondary) {
    if (TextUtils.isValueTrue(tree)) {
      ctx.reportIssue(tree, MESSAGE, secondary);
    }
  }
}
