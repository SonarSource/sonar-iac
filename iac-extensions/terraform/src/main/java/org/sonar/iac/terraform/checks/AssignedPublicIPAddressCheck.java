/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6329")
public class AssignedPublicIPAddressCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using public IP address is safe here.";
  private static final String SECONDARY_INSTANCE_MESSAGE = "Related instance";
  private static final String SECONDARY_TEMPLATE_MESSAGE = "Related template";
  
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    checkDMSReplicationInstance(ctx, resource);
    checkEC2Instance(ctx, resource);
    checkEC2LaunchTemplate(ctx, resource);
  }

  private static void checkDMSReplicationInstance(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_dms_replication_instance")) {
      return;
    }
    Tree resourceType = resource.labels().get(0);
    Optional<AttributeTree> maybePubliclyAccessible = PropertyUtils.get(resource, "publicly_accessible", AttributeTree.class);
    if (maybePubliclyAccessible.isPresent()) {
      AttributeTree publiclyAccessible = maybePubliclyAccessible.get();
      if (TextUtils.isValueTrue(publiclyAccessible.value())) {
        ctx.reportIssue(publiclyAccessible.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_INSTANCE_MESSAGE));
      }
    } else {
      ctx.reportIssue(resourceType, MESSAGE);
    }
  }

  private static void checkEC2Instance(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_instance")) {
      return;
    }
    Tree resourceType = resource.labels().get(0);
    Optional<AttributeTree> maybeAssociatePublicIpAddress = PropertyUtils.get(resource, "associate_public_ip_address", AttributeTree.class);
    if (maybeAssociatePublicIpAddress.isPresent()) {
      AttributeTree associatePublicIpAddress = maybeAssociatePublicIpAddress.get();
      if (TextUtils.isValueTrue(associatePublicIpAddress.value())) {
        ctx.reportIssue(associatePublicIpAddress.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_INSTANCE_MESSAGE));
      }
    } else {
      ctx.reportIssue(resourceType, MESSAGE);
    }
  }

  private static void checkEC2LaunchTemplate(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_launch_template")) {
      return;
    }
    Tree resourceType = resource.labels().get(0);
    Optional<BlockTree> maybeNetworkInterfaces = PropertyUtils.get(resource, "network_interfaces", BlockTree.class);
    if (maybeNetworkInterfaces.isPresent()) {
      BlockTree networkInterfaces = maybeNetworkInterfaces.get();
      Optional<AttributeTree> maybeAssociatePublicIpAddress = PropertyUtils.get(networkInterfaces, "associate_public_ip_address", AttributeTree.class);
      if (maybeAssociatePublicIpAddress.isPresent()) {
        AttributeTree assicuatePublicIpAddress = maybeAssociatePublicIpAddress.get();
        if (TextUtils.isValueTrue(assicuatePublicIpAddress.value())) {
          ctx.reportIssue(assicuatePublicIpAddress.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_TEMPLATE_MESSAGE));
        }
        return;
      }
      ctx.reportIssue(networkInterfaces.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_TEMPLATE_MESSAGE));
      return;
    }
    ctx.reportIssue(resourceType, MESSAGE);
  }
}
