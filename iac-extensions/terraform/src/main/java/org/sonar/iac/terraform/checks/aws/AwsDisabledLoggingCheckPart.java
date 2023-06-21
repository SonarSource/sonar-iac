/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2023 SonarSource SA
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
package org.sonar.iac.terraform.checks.aws;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.sonar.api.utils.Version;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.AbstractNewResourceCheck;
import org.sonar.iac.terraform.symbols.AttributeSymbol;
import org.sonar.iac.terraform.symbols.BlockSymbol;
import org.sonar.iac.terraform.symbols.ListSymbol;
import org.sonar.iac.common.dsl.Symbol;

import static org.sonar.iac.terraform.checks.AbstractResourceCheck.S3_BUCKET;
import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE;
import static org.sonar.iac.terraform.checks.DisabledLoggingCheck.MESSAGE_OMITTING;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.equalTo;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.isFalse;
import static org.sonar.iac.terraform.checks.utils.ExpressionPredicate.notEqualTo;
import static org.sonar.iac.terraform.plugin.TerraformProviders.Provider.Identifier.AWS;

public class AwsDisabledLoggingCheckPart extends AbstractNewResourceCheck {

  private static final Version AWS_V_4 = Version.create(4, 0);

  @Override
  protected void registerResourceConsumer() {
    // https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket
    register(S3_BUCKET, resource -> {
      BlockTree resourceBlock = resource.tree;
      if (resource.provider(AWS).hasVersionLowerThan(AWS_V_4) && !isMaybeLoggingBucket(resourceBlock) && PropertyUtils.isMissing(resourceBlock, "logging")) {
        resource.report(String.format(MESSAGE_OMITTING, "logging or acl=\"log-delivery-write\""));
      }
    });

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
        .filter(Symbol::isPresent)
        .map(l -> l.attribute("enabled"));

      if (logSettings.noneMatch(l -> l.is(isFalse().negate()))) {
        brokerLogs.report(String.format(MESSAGE_OMITTING, "cloudwatch_logs, firehose or s3"));
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
        .reportIfAbsent(String.format(MESSAGE_OMITTING, "logs.audit or logs.general"));

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
        () -> resource.report(String.format(MESSAGE_OMITTING, "log_publishing_options of type \"AUDIT_LOGS\""))));

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
