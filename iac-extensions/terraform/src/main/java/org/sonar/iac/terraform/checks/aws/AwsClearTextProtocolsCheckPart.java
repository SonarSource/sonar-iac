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
package org.sonar.iac.terraform.checks.aws;

import java.util.Set;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_CLEAR_TEXT;
import static org.sonar.iac.terraform.checks.ClearTextProtocolsCheck.MESSAGE_OMITTING;

public class AwsClearTextProtocolsCheckPart extends AbstractResourceCheck {

  private static final Set<String> SENSITIVE_LB_DEFAULT_ACTION_TYPES = Set.of("fixed-response", "forward");

  @Override
  protected void registerResourceChecks() {
    register(AwsClearTextProtocolsCheckPart::checkMskCluster, "aws_msk_cluster");
    register(AwsClearTextProtocolsCheckPart::checkESDomain, "aws_elasticsearch_domain");
    register(AwsClearTextProtocolsCheckPart::checkLbListener, "aws_lb_listener");
    register(AwsClearTextProtocolsCheckPart::checkESReplicationGroup, "aws_elasticache_replication_group");
    register(AwsClearTextProtocolsCheckPart::checkEcsTaskDefinition, "aws_ecs_task_definition");
    register(AwsClearTextProtocolsCheckPart::checkKinesisStream, "aws_kinesis_stream");
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
    PropertyUtils.get(encryptionBlock, "client_broker", AttributeTree.class)
      .filter(clientBroker -> TextUtils.isValue(clientBroker.value(), "TLS").isFalse())
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, MESSAGE_CLEAR_TEXT));
  }

  private static void checkESDomain(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_endpoint_options", BlockTree.class)
      .ifPresent(t -> reportOnFalseProperty(ctx, t, "enforce_https", MESSAGE_CLEAR_TEXT));

    PropertyUtils.get(resource, "node_to_node_encryption", BlockTree.class).ifPresentOrElse(encryption -> reportOnFalseProperty(ctx, encryption, "enabled", MESSAGE_CLEAR_TEXT),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, "node_to_node_encryption")));
  }

  private static void reportOnFalseProperty(CheckContext ctx, Tree tree, String propertyName, String message) {
    PropertyUtils.get(tree, propertyName, AttributeTree.class)
      .ifPresent(inCluster -> reportOnFalse(ctx, inCluster, message));
  }

  private static void checkLbListener(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "protocol", AttributeTree.class)
      .filter(p -> TextUtils.isValue(p.value(), "HTTP").isTrue())
      .ifPresent(rootProtocol -> checkLbDefaultAction(ctx, resource, rootProtocol));
  }

  private static void checkLbDefaultAction(CheckContext ctx, BlockTree resource, Tree rootProtocol) {
    if (PropertyUtils.getAll(resource, "default_action", BlockTree.class).stream()
      .anyMatch(defaultAction -> isInsecureRedirect(defaultAction) || isSensitiveAction(defaultAction))) {
      ctx.reportIssue(rootProtocol, MESSAGE_CLEAR_TEXT);
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
    PropertyUtils.get(resource, "transit_encryption_enabled", AttributeTree.class).ifPresentOrElse(encryption -> reportOnFalse(ctx, encryption, MESSAGE_CLEAR_TEXT),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, "transit_encryption_enabled")));
  }

  private static void checkEcsTaskDefinition(CheckContext ctx, BlockTree resource) {
    PropertyUtils.getAll(resource, "volume", BlockTree.class)
      .forEach(volume -> PropertyUtils.get(volume, "efs_volume_configuration", BlockTree.class).ifPresent(config -> checkEscVolumeConfig(ctx, config)));
  }

  private static void checkEscVolumeConfig(CheckContext ctx, BlockTree config) {
    PropertyUtils.get(config, "transit_encryption", AttributeTree.class).ifPresentOrElse(encryption -> reportSensitiveValue(ctx, encryption, "DISABLED", MESSAGE_CLEAR_TEXT),
      () -> ctx.reportIssue(config.key(), String.format(MESSAGE_OMITTING, "transit_encryption")));
  }

  private static void checkKinesisStream(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "encryption_type", AttributeTree.class).ifPresentOrElse(encryption -> reportSensitiveValue(ctx, encryption, "NONE", MESSAGE_CLEAR_TEXT),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, "encryption_type")));
  }
}
