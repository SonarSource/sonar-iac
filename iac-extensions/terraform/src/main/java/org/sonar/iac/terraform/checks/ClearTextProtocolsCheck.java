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

import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractResourceCheck {

  private static final String MESSAGE_PROTOCOL_FORMAT = "Using %s protocol is insecure. Use %s instead.";
  private static final String MESSAGE_CLEAR_TEXT = "Make sure allowing clear-text traffic is safe here.";

  private static final Set<String> SENSITIVE_LB_DEFAULT_ACTION_TYPES = Set.of("fixed-response", "forward");

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_msk_cluster")) {
      checkMskCluster(ctx, resource);
    } else if (isResource(resource, "aws_elasticsearch_domain")) {
      checkESDomain(ctx, resource);
    } else if (isResource(resource, "aws_lb_listener")) {
      checkLbListener(ctx, resource);
    } else if (isResource(resource, "aws_elasticache_replication_group")) {
      checkESReplicationGroup(ctx, resource);
    } else if (isResource(resource, "aws_ecs_task_definition")) {
      checkEcsTaskDefinition(ctx, resource);
    } else if (isResource(resource, "aws_kinesis_stream")) {
      checkKinesisStream(ctx, resource);
    }
  }

  private static void checkMskCluster(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "encryption_info", BlockTree.class)
      .flatMap(e -> PropertyUtils.get(e, "encryption_in_transit", BlockTree.class))
      .ifPresent(e -> {
        checkMskClientBroker(ctx, e);
        reportOnFalseProperty(ctx, e, "in_cluster", MESSAGE_CLEAR_TEXT);
      });
  }

  private static void checkMskClientBroker(CheckContext ctx, BlockTree encryptionBlock) {
    PropertyUtils.value(encryptionBlock, "client_broker", LiteralExprTree.class)
      .filter(clientBroker -> !"TLS".equals(clientBroker.value()))
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, String.format(MESSAGE_PROTOCOL_FORMAT, clientBroker.value(), "TLS")));
  }

  private static void checkESDomain(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_endpoint_options", BlockTree.class)
      .ifPresent(t -> reportOnFalseProperty(ctx, t, "enforce_https", String.format(MESSAGE_PROTOCOL_FORMAT, "HTTP", "HTTPS")));

    PropertyUtils.get(resource, "node_to_node_encryption", BlockTree.class)
      .ifPresentOrElse(x -> reportOnFalseProperty(ctx, x, "enabled", MESSAGE_CLEAR_TEXT),
        () -> ctx.reportIssue(resource.labels().get(0), MESSAGE_CLEAR_TEXT));
  }

  private static void reportOnFalseProperty(CheckContext ctx, Tree tree, String propertyName, String message) {
    PropertyUtils.value(tree, propertyName, LiteralExprTree.class)
      .filter(TextUtils::isValueFalse)
      .ifPresent(inCluster -> ctx.reportIssue(inCluster, message));
  }

  private static void checkLbListener(CheckContext ctx, BlockTree resource) {
    PropertyUtils.value(resource, "protocol").filter(p -> TextUtils.isValue(p,"HTTP").isTrue())
      .ifPresent(rootProtocol -> checkLbDefaultAction(ctx, resource, rootProtocol));
  }

  private static void checkLbDefaultAction(CheckContext ctx, BlockTree resource, Tree rootProtocol) {
    if (PropertyUtils.getAll(resource, "default_action", BlockTree.class).stream()
      .anyMatch(defaultAction -> isInsecureRedirect(defaultAction) || isSensitiveAction(defaultAction))) {
      ctx.reportIssue(rootProtocol, String.format(MESSAGE_PROTOCOL_FORMAT, "HTTP", "HTTPS"));
    }
  }

  private static boolean isInsecureRedirect(BlockTree defaultAction) {
    return PropertyUtils.get(defaultAction, "redirect", BlockTree.class)
      .flatMap(redirect -> PropertyUtils.value(redirect, "protocol"))
      .filter(protocol -> TextUtils.isValue(protocol, "HTTP").isTrue())
      .isPresent();
  }

  private static boolean isSensitiveAction(BlockTree defaultAction) {
    return PropertyUtils.value(defaultAction, "type")
      .filter(type -> TextUtils.matchesValue(type, SENSITIVE_LB_DEFAULT_ACTION_TYPES::contains).isTrue())
      .isPresent();
  }

  private static void checkESReplicationGroup(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.has(resource, "transit_encryption_enabled").isFalse()) {
      reportResource(ctx, resource, MESSAGE_CLEAR_TEXT);
    } else {
      reportOnFalseProperty(ctx, resource, "transit_encryption_enabled", MESSAGE_CLEAR_TEXT);
    }
  }

  private static void checkEcsTaskDefinition(CheckContext ctx, BlockTree resource) {
    PropertyUtils.getAll(resource, "volume", BlockTree.class).forEach(volume ->
        PropertyUtils.get(volume, "efs_volume_configuration", BlockTree.class).ifPresent(config ->
          checkEscVolumeConfig(ctx, config)));
  }

  private static void checkEscVolumeConfig(CheckContext ctx, BlockTree config) {
    if (PropertyUtils.has(config, "transit_encryption").isFalse()) {
      ctx.reportIssue(config.key(), MESSAGE_CLEAR_TEXT);
    } else {
      PropertyUtils.value(config, "transit_encryption")
        .filter(encryption -> TextUtils.isValue(encryption, "DISABLED").isTrue())
        .ifPresent(e -> ctx.reportIssue(e, MESSAGE_CLEAR_TEXT));
    }
  }
  
  private static void checkKinesisStream(CheckContext ctx, BlockTree resource) {
    if (PropertyUtils.has(resource, "encryption_type").isFalse()) {
      reportResource(ctx, resource, MESSAGE_CLEAR_TEXT);
    } else {
      PropertyUtils.value(resource, "encryption_type")
        .filter(type -> TextUtils.isValue(type, "NONE").isTrue())
        .ifPresent(t -> ctx.reportIssue(t, MESSAGE_CLEAR_TEXT));
    }
  }

}
