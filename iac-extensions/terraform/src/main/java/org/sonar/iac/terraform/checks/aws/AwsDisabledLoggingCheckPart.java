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
package org.sonar.iac.terraform.checks.aws;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.TextTree;
import org.sonar.iac.common.checkdsl.ContextualTree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeAccessTree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.FileTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.StatementTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.AbstractNewCrossResourceCheck;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;
import org.sonar.iac.terraform.symbols.ListSymbol;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

import static java.util.stream.Collectors.toMap;
import static org.sonar.iac.terraform.checks.AbstractResourceCheck.S3_BUCKET;
import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE;
import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE_OMITTING;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AWS;

public class AwsDisabledLoggingCheckPart extends AbstractNewCrossResourceCheck {

  private static final Version AWS_V_4 = Version.create(4, 0);

  private Map<String, List<BlockTree>> typeToTree = new HashMap<>();
  private Map<String, BlockTree> iamPolicyDocuments = new HashMap<>();

  @Override
  public void initialize(InitContext init) {
    init.register(FileTree.class, (CheckContext ctx, FileTree tree) -> init(tree));

    super.initialize(init);
  }

  private void init(FileTree tree) {
    typeToTree = tree.properties().stream()
      .filter(BlockTree.class::isInstance)
      .map(BlockTree.class::cast)
      .filter(AbstractNewResourceCheck::isResource)
      .collect(Collectors.groupingBy(AbstractResourceCheck::getResourceType, Collectors.mapping(e -> e, Collectors.toList())));
    iamPolicyDocuments = tree.properties().stream()
      .filter(BlockTree.class::isInstance)
      .map(BlockTree.class::cast)
      .filter(block -> isDataOfType(block, "aws_iam_policy_document"))
      // In theory, a valid Terraform file should not contain two iam policy document blocks with the same name.
      // This check is to be on the safe side.
      .collect(toMap(AbstractResourceCheck::getReferenceLabel, Function.identity(), (block1, block2) -> block1));
  }

  @Override
  protected void registerResourceConsumer() {
    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
    register(S3_BUCKET, this::s3BucketConsumer);

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_stage
    register("aws_api_gateway_stage", resource -> resource.attribute("xray_tracing_enabled")
      .reportIf(isFalse(), MESSAGE)
      .reportIfAbsent(MESSAGE_OMITTING));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/api_gateway_stage
    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/apigatewayv2_api
    register(Set.of("aws_apigatewayv2_stage", "aws_api_gateway_stage"), resource -> resource.block("access_log_settings")
      .reportIfAbsent(MESSAGE_OMITTING));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/msk_cluster
    register("aws_msk_cluster", resource -> {
      BlockSymbol brokerLogs = resource.block("logging_info")
        .reportIfAbsent(String.format(MESSAGE_OMITTING, "logging_info.broker_logs"))
        .block("broker_logs")
        .reportIfAbsent(MESSAGE_OMITTING);

      Stream<AttributeSymbol> logSettings = Stream.of("cloudwatch_logs", "firehose", "s3")
        .map(brokerLogs::block)
        .filter(ContextualTree::isPresent)
        .map(l -> l.attribute("enabled"));

      if (logSettings.noneMatch(l -> l.is(isFalse().negate()))) {
        brokerLogs.report(String.format(MESSAGE_OMITTING, "cloudwatch_logs\", \"firehose\" or \"s3"));
      }
    });

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/neptune_cluster
    register("aws_neptune_cluster", resource -> resource.list("enable_cloudwatch_logs_exports")
      .reportIfEmpty(MESSAGE)
      .reportIfAbsent(MESSAGE_OMITTING));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/docdb_cluster
    register("aws_docdb_cluster", resource -> {
      ListSymbol exports = resource.list("enabled_cloudwatch_logs_exports")
        .reportIfAbsent(MESSAGE_OMITTING);

      if (!exports.isByReference() && exports.getItemIf(equalTo("audit")).findAny().isEmpty()) {
        exports.report(MESSAGE);
      }
    });

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/mq_broker
    register("aws_mq_broker", resource -> {
      BlockSymbol logs = resource.block("logs")
        .reportIfAbsent(String.format(MESSAGE_OMITTING, "logs.audit\" or \"logs.general"));

      AttributeSymbol auditLog = logs.attribute("audit");
      AttributeSymbol generalLog = logs.attribute("general");
      if ((auditLog.isAbsent() && generalLog.isAbsent()) || (auditLog.is(isFalse()) && generalLog.is(isFalse()))) {
        logs.report(MESSAGE);
      }
    });

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/redshift_cluster
    register("aws_redshift_cluster", resource -> resource.block("logging")
      .reportIfAbsent(String.format(MESSAGE_OMITTING, "logging.enable"))
      .attribute("enable")
      .reportIf(isFalse(), MESSAGE)
      .reportIfAbsent(MESSAGE));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/globalaccelerator_accelerator
    register("aws_globalaccelerator_accelerator", resource -> resource.block("attributes")
      .reportIfAbsent(String.format(MESSAGE_OMITTING, "attributes.flow_logs_enabled"))
      .attribute("flow_logs_enabled")
      .reportIf(isFalse(), MESSAGE)
      .reportIfAbsent(MESSAGE));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elasticsearch_domain
    register("aws_elasticsearch_domain", resource -> resource.blocks("log_publishing_options")
      .filter(block -> block.attribute("log_type").is(notEqualTo("AUDIT_LOGS").negate()))
      .findFirst()
      .ifPresentOrElse(auditLog -> auditLog.attribute("enabled").reportIf(isFalse(), MESSAGE),
        () -> resource.report(String.format(MESSAGE_OMITTING, "log_publishing_options\" of type \"AUDIT_LOGS"))));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/cloudfront_distribution
    register("aws_cloudfront_distribution", resource -> resource.block("logging_config")
      .reportIfAbsent(MESSAGE_OMITTING));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/lb
    register("aws_lb", resource -> resource.block("access_logs")
      .reportIfAbsent(MESSAGE_OMITTING)
      .attribute("enabled")
      .reportIf(isFalse(), MESSAGE)
      .reportIfAbsent(MESSAGE));

    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elb
    register("aws_elb", resource -> resource.block("access_logs")
      .reportIfAbsent(MESSAGE_OMITTING)
      .attribute("enabled")
      .reportIf(isFalse(), MESSAGE));
  }

  private void s3BucketConsumer(ResourceSymbol resource) {
    BlockTree resourceBlock = resource.tree;
    if (resource.provider(AWS).hasVersionLowerThan(AWS_V_4) || resource.provider(AWS).isUnknown()) {
      if (!isMaybeLoggingBucket(resourceBlock) && PropertyUtils.isMissing(resourceBlock, "logging")) {
        resource.report(String.format(MESSAGE_OMITTING, "logging\" or acl=\"log-delivery-write"));
      }
    } else {
      var resourceName = resource.name;
      if (!hasBucketLogging(resourceName) && !hasBucketPolicy(resource)) {
        resource.report(MESSAGE);
      }
    }
  }

  private boolean hasBucketLogging(String resourceName) {
    var bucketLoggins = typeToTree.get("aws_s3_bucket_logging");
    if (bucketLoggins != null) {
      return bucketLoggins.stream().anyMatch(bucketLogging -> hasReferencesInBucketOrTargetBucket(resourceName, bucketLogging));
    }
    return false;
  }

  private static boolean hasReferencesInBucketOrTargetBucket(String resourceName, BlockTree blockTree) {
    return PropertyUtils.get(blockTree, Set.of("bucket", "target_bucket"), AttributeTree.class)
      .stream().anyMatch(block -> hasReferenceToS3Bucket(resourceName, block));
  }

  private static boolean hasReferenceToS3Bucket(String resourceName, StatementTree block) {
    if (block.value() instanceof AttributeAccessTree accessTree &&
      accessTree.object() instanceof AttributeAccessTree accessTreeNested) {
      return TextUtils.isValue(accessTreeNested.object(), "aws_s3_bucket").isTrue() &&
        TextUtils.isValue(accessTreeNested.attribute(), resourceName).isTrue();
    }
    return false;
  }

  private boolean hasBucketPolicy(ResourceSymbol resource) {
    var bucketPolicies = typeToTree.get("aws_s3_bucket_policy");
    if (bucketPolicies != null) {
      return bucketPolicies.stream()
        .filter(block -> PropertyUtils.get(block, "bucket", AttributeTree.class)
          .filter(block2 -> hasReferenceToS3Bucket(resource.name, block2))
          .isPresent())
        .anyMatch(block -> PropertyUtils.get(block, "policy", AttributeTree.class)
          .map(this::toPolicyDocumentData)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .stream().anyMatch(data -> hasPolicyDocumentDataSafe(data, resource.ctx)));
    }
    return false;
  }

  private Optional<BlockTree> toPolicyDocumentData(AttributeTree policyAttributeTree) {
    if (policyAttributeTree.value() instanceof AttributeAccessTree accessTree &&
      accessTree.object() instanceof AttributeAccessTree accessTreeNested &&
      accessTreeNested.object() instanceof AttributeAccessTree attributeAccessTree &&
      TextUtils.isValue(attributeAccessTree.object(), "data").isTrue()) {
      return TextUtils.getValue(accessTreeNested.attribute())
        .map(name -> iamPolicyDocuments.get(name));
    }
    return Optional.empty();
  }

  private static boolean hasPolicyDocumentDataSafe(BlockTree policyDocumentData, CheckContext ctx) {
    var principals = BlockSymbol.fromPresent(ctx, policyDocumentData, null)
      .block("statement")
      .block("principals");
    var type = principals.attribute("type");
    if (type.is(t -> TextUtils.isValue(t, "Service").isTrue())) {
      var identifiers = principals.attribute("identifiers");
      var literals = getLiterals(identifiers);
      return literals.contains("logging.s3.amazonaws.com");
    }
    return false;
  }

  private static List<String> getLiterals(AttributeSymbol attributeSymbol) {
    if (attributeSymbol.isPresent()) {
      return attributeSymbol.tree.value().children().stream()
        .filter(TextTree.class::isInstance)
        .map(TextTree.class::cast)
        .map(TextTree::value)
        .toList();
    }
    return List.of();
  }

  private static boolean isMaybeLoggingBucket(BlockTree resource) {
    Optional<AttributeTree> acl = PropertyUtils.get(resource, "acl", AttributeTree.class);
    if (acl.isEmpty()) {
      return false;
    }
    ExpressionTree aclValue = acl.get().value();
    if (aclValue.is(TerraformTree.Kind.STRING_LITERAL)) {
      return ((LiteralExprTree) aclValue).value().equals("log-delivery-write");
    }
    return true;
  }
}
