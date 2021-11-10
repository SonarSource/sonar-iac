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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

@Rule(key = "S6258")
public class DisabledLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that disabling logging is safe here.";

  private static final List<String> MSK_LOGGER = Arrays.asList("cloudwatch_logs", "firehose", "s3");

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isS3Bucket(resource)) {
      checkS3Bucket(ctx, resource);
    } else if (isResource(resource, "aws_api_gateway_stage")) {
      checkApiGatewayStage(ctx, resource);
    } else if (isResource(resource, "aws_api_gatewayv2_stage")) {
      checkApiGateway2Stage(ctx, resource);
    } else if (isResource(resource, "aws_msk_cluster")) {
      checkMskCluster(ctx, resource);
    }
  }

  private static void checkS3Bucket(CheckContext ctx, BlockTree resource) {
    if (!isMaybeLoggingBucket(resource) && !PropertyUtils.has(resource, "logging").isTrue()) {
      reportResource(ctx, resource, MESSAGE);
    }
  }

  private static boolean isMaybeLoggingBucket(BlockTree resource) {
    Optional<AttributeTree> acl = PropertyUtils.get(resource, "acl", AttributeTree.class);
    if (acl.isEmpty()) {
      return false;
    }
    ExpressionTree aclValue = acl.get().value();
    if (aclValue.is(Kind.STRING_LITERAL)) {
      return ((LiteralExprTree) aclValue).value().equals("log-delivery-write");
    }
    return true;
  }

  private static void checkApiGatewayStage(CheckContext ctx, BlockTree resource) {
    PropertyUtils.value(resource, "xray_tracing_enabled")
      .ifPresentOrElse(tracing -> reportOnFalse(ctx, tracing, MESSAGE),
        () -> reportResource(ctx, resource, MESSAGE));

    // check also presence of access_log_settings
    checkApiGateway2Stage(ctx, resource);
  }

  private static void checkApiGateway2Stage(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.missing(resource, "access_log_settings")) {
      reportResource(ctx, resource, MESSAGE);
    }
  }

  private static void checkMskCluster(CheckContext ctx, BlockTree resource) {
    // look for logging_info::broker_logs, raise issue on certain parent if property is not set
    PropertyUtils.get(resource, "logging_info", BlockTree.class)
      .ifPresentOrElse(info -> PropertyUtils.get(info, "broker_logs", BlockTree.class)
        .ifPresentOrElse(logs -> checkMskLogs(ctx, logs), () -> ctx.reportIssue(info, MESSAGE)),
        () -> reportResource(ctx, resource, MESSAGE));
  }

  private static void checkMskLogs(CheckContext ctx, Tree logs) {
    // raise issue if none of the logger is enabled
    if (MSK_LOGGER.stream()
      .noneMatch(name -> PropertyUtils.get(logs, name, BlockTree.class)
        .filter(DisabledLoggingCheck::isLogEnabled).isPresent())) {
      ctx.reportIssue(logs, MESSAGE);
    }
  }

  private static boolean isLogEnabled(BlockTree logger) {
    return PropertyUtils.value(logger, "enabled")
      .filter(TextUtils::isValueFalse)
      .isEmpty();
  }
}
