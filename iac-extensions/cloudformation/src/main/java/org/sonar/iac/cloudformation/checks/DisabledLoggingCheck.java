/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.tree.FunctionCallTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.XPathUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S6258")
public class DisabledLoggingCheck extends AbstractCrossResourceCheck {

  private static final String MESSAGE = "Make sure that disabling logging is safe here.";
  private static final List<String> MSK_LOGGER = Arrays.asList("CloudWatchLogs", "Firehose", "S3");
  private static final String MESSAGE_OMITTING_FORMAT = "Omitting \"%s\" makes logs incomplete. Make sure it is safe here.";
  private static final String ENABLED = "Enabled";
  private static final String ENABLE_CLOUDWATCH_LOGS_EXPORTS_KEY = "EnableCloudwatchLogsExports";

  private Set<String> s3bucketsWithBucketPolicy;

  @Override
  protected void beforeCheckResource() {
    s3bucketsWithBucketPolicy = resourceNameToResource.values().stream()
      .filter(resource -> resource.isType("AWS::S3::BucketPolicy"))
      .filter(DisabledLoggingCheck::hasLoggingBucketPolicy)
      .map(DisabledLoggingCheck::bucketNameForBucketPolicy)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toSet());
  }

  private static boolean hasLoggingBucketPolicy(Resource resource) {
    if (resource.properties() != null) {
      return XPathUtils.getTrees(resource.properties(), "/PolicyDocument/Statement[]/Principal/Service").stream()
        .filter(ScalarTree.class::isInstance)
        .map(ScalarTree.class::cast)
        .anyMatch(tree -> "logging.s3.amazonaws.com".equals(tree.value()));
    }
    return false;
  }

  private static Optional<String> bucketNameForBucketPolicy(Resource resource) {
    return PropertyUtils.get(resource.properties(), "Bucket")
      .filter(TupleTree.class::isInstance)
      .map(TupleTree.class::cast)
      .map(TupleTree::value)
      .filter(FunctionCallTree.class::isInstance)
      .map(FunctionCallTree.class::cast)
      .filter(functionCallTree -> "Ref".equals(functionCallTree.name()))
      .map(functionCallTree -> functionCallTree.arguments().get(0))
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast)
      .map(ScalarTree::value);
  }

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
    } else if (resource.isType("AWS::Neptune::DBCluster")) {
      checkNeptuneDbCluster(ctx, resource);
    } else if (resource.isType("AWS::DocDB::DBCluster")) {
      checkDocDbCluster(ctx, resource);
    } else if (resource.isType("AWS::AmazonMQ::Broker")) {
      checkAmazonMQBroker(ctx, resource);
    } else if (resource.isType("AWS::Redshift::Cluster")) {
      checkRedshiftCluster(ctx, resource);
    } else if (resource.isType("AWS::Elasticsearch::Domain") || resource.isType("AWS::OpenSearchService::Domain")) {
      checkSearchDomain(ctx, resource);
    } else if (resource.isType("AWS::CloudFront::Distribution")) {
      checkCloudFrontDistribution(ctx, resource);
    } else if (resource.isType("AWS::ElasticLoadBalancing::LoadBalancer")) {
      checkElasticLoadBalancer(ctx, resource);
    } else if (resource.isType("AWS::ElasticLoadBalancingV2::LoadBalancer")) {
      checkElasticLoadBalancerV2(ctx, resource);
    }
  }

  private void checkS3Bucket(CheckContext ctx, Resource resource) {
    YamlTree properties = resource.properties();
    if (PropertyUtils.value(properties, "LoggingConfiguration").isEmpty() &&
      !isMaybeLoggingBucket(properties) &&
      !s3bucketsWithBucketPolicy.contains(resource.name().value())) {
      ctx.reportIssue(resource.type(), omittingMessage("LoggingConfiguration"));
    }
  }

  private static boolean isMaybeLoggingBucket(@Nullable YamlTree properties) {
    Optional<Tree> acl = PropertyUtils.value(properties, "AccessControl");
    if (acl.isPresent()) {
      Optional<String> scalarValue = TextUtils.getValue(acl.get());
      return scalarValue.map(s -> s.equals("LogDeliveryWrite")).orElse(true);
    }
    return false;
  }

  private static void checkApiGatewayStage(CheckContext ctx, Resource resource) {
    YamlTree properties = resource.properties();
    PropertyUtils.value(properties, "TracingEnabled").ifPresentOrElse(f -> reportOnFalse(ctx, f),
      () -> reportResource(ctx, resource, omittingMessage("TracingEnabled")));
    reportOnMissingProperty(ctx, properties, "AccessLogSetting", resource.type());
  }

  private static void checkApiGatewayV2Stage(CheckContext ctx, Resource resource) {
    reportOnMissingProperty(ctx, resource.properties(), "AccessLogSettings", resource.type());
  }

  private static void checkMskCluster(CheckContext ctx, Resource resource) {
    // look for LoggingInfo::BrokerLogs, raise issue on certain parent if property is not set
    PropertyUtils.get(resource.properties(), "LoggingInfo")
      .ifPresentOrElse(info -> PropertyUtils.get(info.value(), "BrokerLogs")
        .ifPresentOrElse(logs -> checkMskLogs(ctx, logs), () -> ctx.reportIssue(info.key(), omittingMessage("BrokerLogs"))),
        () -> reportResource(ctx, resource, omittingMessage("LoggingInfo")));
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
    return PropertyUtils.value(logger, ENABLED)
      .filter(TextUtils::isValueFalse)
      .isEmpty();
  }

  private static void checkNeptuneDbCluster(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), ENABLE_CLOUDWATCH_LOGS_EXPORTS_KEY).ifPresentOrElse(exportsValue -> {
      if (exportsValue instanceof SequenceTree sequenceTree && sequenceTree.elements().isEmpty()) {
        ctx.reportIssue(exportsValue, MESSAGE);
      }
    }, () -> reportResource(ctx, resource, omittingMessage(ENABLE_CLOUDWATCH_LOGS_EXPORTS_KEY)));
  }

  private static void checkDocDbCluster(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), ENABLE_CLOUDWATCH_LOGS_EXPORTS_KEY).ifPresentOrElse(exportsProperty -> {
      if (exportsProperty.value() instanceof SequenceTree sequenceTree && containsOnlyStringsWithoutAudit(sequenceTree)) {
        ctx.reportIssue(exportsProperty.key(), MESSAGE);
      }
    }, () -> reportResource(ctx, resource, omittingMessage(ENABLE_CLOUDWATCH_LOGS_EXPORTS_KEY)));
  }

  private static boolean containsOnlyStringsWithoutAudit(SequenceTree exports) {
    return exports.elements().stream().allMatch(
      export -> export.metadata().tag().endsWith("str") && TextUtils.isValue(export, "audit").isFalse());
  }

  private static void checkAmazonMQBroker(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "Logs").ifPresentOrElse(logs -> {
      if (logs.value() instanceof MappingTree mappingTree && containsOnlyFalse(mappingTree)) {
        ctx.reportIssue(logs.key(), MESSAGE);
      }
    }, () -> reportResource(ctx, resource, MESSAGE));
  }

  private static boolean containsOnlyFalse(MappingTree logs) {
    return logs.elements().stream().map(TupleTree::value).allMatch(TextUtils::isValueFalse);
  }

  private static void checkRedshiftCluster(CheckContext ctx, Resource resource) {
    if (PropertyUtils.isMissing(resource.properties(), "LoggingProperties")) {
      reportResource(ctx, resource, omittingMessage("LoggingProperties"));
    }
  }

  private static void checkSearchDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "LogPublishingOptions").ifPresentOrElse(logs -> checkEnabledAuditLogAvailability(ctx, logs),
      () -> reportResource(ctx, resource, omittingMessage("LogPublishingOptions")));
  }

  private static void checkEnabledAuditLogAvailability(CheckContext ctx, PropertyTree logs) {
    PropertyUtils.value(logs.value(), "AUDIT_LOGS").flatMap(v -> PropertyUtils.value(v, ENABLED))
      .ifPresentOrElse(auditLogsEnable -> reportOnFalse(ctx, auditLogsEnable),
        () -> ctx.reportIssue(logs.key(), omittingMessage("AUDIT_LOGS")));
  }

  private static void checkCloudFrontDistribution(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "DistributionConfig").ifPresentOrElse(config -> reportOnMissingProperty(ctx, config.value(), "Logging", config.key()),
      () -> reportResource(ctx, resource, omittingMessage("DistributionConfig")));
  }

  private static void checkElasticLoadBalancer(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "AccessLoggingPolicy").ifPresentOrElse(policy -> PropertyUtils.value(policy, ENABLED).ifPresent(e -> reportOnFalse(ctx, e)),
      () -> reportResource(ctx, resource, omittingMessage("AccessLoggingPolicy")));
  }

  private static void checkElasticLoadBalancerV2(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "LoadBalancerAttributes").ifPresentOrElse(
      attributes -> getAccessLogsAttribute(attributes.value()).ifPresentOrElse(value -> reportOnFalse(ctx, value), () -> ctx.reportIssue(attributes.key(), MESSAGE)),
      () -> reportResource(ctx, resource, omittingMessage("LoadBalancerAttributes")));
  }

  private static Optional<Tree> getAccessLogsAttribute(Tree attributes) {
    if (attributes instanceof SequenceTree sequenceTree) {
      return sequenceTree.elements().stream()
        .filter(attribute -> TextUtils.isValue(PropertyUtils.valueOrNull(attribute, "Key"), "access_logs.s3.enabled").isTrue())
        .map(attribute -> PropertyUtils.value(attribute, "Value")).findFirst().orElse(Optional.empty());
    }
    return Optional.empty();
  }

  private static void reportOnMissingProperty(CheckContext ctx, @Nullable Tree properties, String property, Tree raiseOn) {
    if (PropertyUtils.isMissing(properties, property)) {
      ctx.reportIssue(raiseOn, omittingMessage(property));
    }
  }

  private static void reportOnFalse(CheckContext ctx, Tree tree) {
    if (TextUtils.isValueFalse(tree)) {
      ctx.reportIssue(tree, MESSAGE);
    }
  }

  private static String omittingMessage(String missingProperty) {
    return String.format(MESSAGE_OMITTING_FORMAT, missingProperty);
  }
}
