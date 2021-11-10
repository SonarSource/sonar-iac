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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.CloudformationTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6258")
public class DisabledLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that disabling logging is safe here.";
  private static final List<String> MSK_LOGGER = Arrays.asList("CloudWatchLogs", "Firehose", "S3");

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (isS3Bucket(resource)) {
      checkS3Bucket(ctx, resource);
    } else if (resource.isType("AWS::ApiGateway::Stage")) {
      checkApiGatewayStage(ctx, resource);
    } else if (resource.isType("AWS::ApiGatewayV2::Stage")) {
      checkApiGatewayV2Stage(ctx, resource);
    } else if (resource.isType("AWS::MSK::Cluster")) {
      checkMskCluster(ctx, resource);
    }
  }

  private static void checkS3Bucket(CheckContext ctx, Resource resource) {
    CloudformationTree properties = resource.properties();
    if (PropertyUtils.value(properties, "LoggingConfiguration").isEmpty() && !isMaybeLoggingBucket(properties)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static boolean isMaybeLoggingBucket(@Nullable CloudformationTree properties) {
    Optional<Tree> acl = PropertyUtils.value(properties, "AccessControl");
    if (acl.isPresent()) {
      Optional<String> scalarValue = TextUtils.getValue(acl.get());
      return scalarValue.map(s -> s.equals("LogDeliveryWrite")).orElse(true);
    }
    return false;
  }

  private static void checkApiGatewayStage(CheckContext ctx, Resource resource) {
    CloudformationTree properties = resource.properties();
    PropertyUtils.value(properties, "TracingEnabled").ifPresentOrElse(f -> reportOnFalse(ctx, f),
      () -> reportResource(ctx, resource, MESSAGE));
    reportOnMissingProperty(ctx, properties, "AccessLogSetting", resource.type());
  }

  private static void checkApiGatewayV2Stage(CheckContext ctx, Resource resource) {
    reportOnMissingProperty(ctx, resource.properties(), "AccessLogSettings", resource.type());
  }

  private static void checkMskCluster(CheckContext ctx, Resource resource) {
    // look for LoggingInfo::BrokerLogs, raise issue on certain parent if property is not set
    PropertyUtils.get(resource.properties(), "LoggingInfo")
      .ifPresentOrElse(info -> PropertyUtils.get(info.value(), "BrokerLogs")
          .ifPresentOrElse(logs -> checkMskLogs(ctx, logs), () -> ctx.reportIssue(info.key(), MESSAGE)),
        () -> reportResource(ctx, resource, MESSAGE));
  }

  private static void checkMskLogs(CheckContext ctx, PropertyTree logs) {
    // raise issue if none of the logger is enabled
    if (MSK_LOGGER.stream()
      .noneMatch(name -> PropertyUtils.value(logs.value(), name)
        .filter(DisabledLoggingCheck::isLogEnabled).isPresent())) {
      ctx.reportIssue(logs.key(), MESSAGE);
    }
  }

  private static boolean isLogEnabled(Tree logger) {
    return PropertyUtils.value(logger, "Enabled")
      .filter(TextUtils::isValueFalse)
      .isEmpty();
  }

  private static void reportOnMissingProperty(CheckContext ctx, @Nullable Tree properties, String property, Tree raiseOn) {
    if (PropertyUtils.has(properties, property).isFalse()) {
      ctx.reportIssue(raiseOn, MESSAGE);
    }
  }

  private static void reportOnFalse(CheckContext ctx, Tree tree) {
    if (TextUtils.isValueFalse(tree)) {
      ctx.reportIssue(tree, MESSAGE);
    }
  }
}
