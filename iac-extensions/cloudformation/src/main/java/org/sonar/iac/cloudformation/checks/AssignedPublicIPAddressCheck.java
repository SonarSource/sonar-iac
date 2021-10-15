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
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6329")
public class AssignedPublicIPAddressCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using public IP address is safe here.";
  private static final String SECONDARY_DMS_MESSAGE = "Related DMS instance";
  private static final String SECONDARY_EC2_INSTANCE_MESSAGE = "Related EC2 instance";
  private static final String SECONDARY_EC2_TEMPLATE_MESSAGE = "Related EC2 template";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    checkDMSReplicationInstance(ctx, resource);
    checkEC2Instance(ctx, resource);
    checkEC2LaunchTemplate(ctx, resource);
  }

  private static void checkDMSReplicationInstance(CheckContext ctx, Resource resource) {
    checkForProperty(
      ctx,
      resource,
      "AWS::DMS::ReplicationInstance",
      "PubliclyAccessible",
      TextUtils::isValueTrue,
      PropertyTree::value,
      SECONDARY_DMS_MESSAGE);
  }

  private static void checkEC2Instance(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::EC2::Instance")) {
      return;
    }
    if (!checkEC2NetworkInterfaces(ctx, resource, resource.properties(), SECONDARY_EC2_INSTANCE_MESSAGE)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static void checkEC2LaunchTemplate(CheckContext ctx, Resource resource) {
    checkForProperty(
      ctx,
      resource,
      "AWS::EC2::LaunchTemplate",
      "LaunchTemplateData",
      t -> !checkEC2NetworkInterfaces(ctx, resource, t, SECONDARY_EC2_TEMPLATE_MESSAGE),
      PropertyTree::key,
      SECONDARY_EC2_TEMPLATE_MESSAGE);
  }

  private static boolean checkEC2NetworkInterfaces(CheckContext ctx, Resource resource, @Nullable Tree tree, String secondaryMessage) {
    Optional<PropertyTree> maybeNetworkInterfaces = PropertyUtils.get(tree, "NetworkInterfaces");
    if (maybeNetworkInterfaces.isPresent()) {
      PropertyTree networkInterfaces = maybeNetworkInterfaces.get();
      if (networkInterfaces.value() instanceof SequenceTree) {
        Optional<PropertyTree> maybeAssociatePublicIpAddress = ((SequenceTree) networkInterfaces.value()).elements().stream()
          .map(e -> PropertyUtils.get(e, "AssociatePublicIpAddress"))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findAny();
        if (maybeAssociatePublicIpAddress.isPresent()) {
          PropertyTree associatePublicIpAddress = maybeAssociatePublicIpAddress.get();
          if (TextUtils.isValueTrue(associatePublicIpAddress.value())) {
            ctx.reportIssue(associatePublicIpAddress.value(), MESSAGE, new SecondaryLocation(resource.type(), secondaryMessage));
          }
          return true;
        }
      }
      ctx.reportIssue(networkInterfaces.key(), MESSAGE, new SecondaryLocation(resource.type(), secondaryMessage));
      return true;
    }
    return false;
  }

  private static void checkForProperty(
    CheckContext ctx,
    Resource resource,
    String resourceName,
    String propertyName,
    Predicate<Tree> propertyTest,
    Function<PropertyTree, Tree> treeToHighlight,
    String secondaryMessage) {

    if (!resource.isType(resourceName)) {
      return;
    }
    Optional<PropertyTree> property = PropertyUtils.get(resource.properties(), propertyName);
    if (property.isPresent()) {
      PropertyTree propertyTree = property.get();
      if (propertyTest.test(propertyTree.value())) {
        ctx.reportIssue(treeToHighlight.apply(propertyTree), MESSAGE, new SecondaryLocation(resource.type(), secondaryMessage));
      }
    } else {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }
}
