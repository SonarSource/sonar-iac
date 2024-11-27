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

import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.XPathUtils;
import org.sonar.iac.common.yaml.tree.MappingTree;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractResourceCheck {

  public static final String MESSAGE_CLEAR_TEXT = "Make sure allowing clear-text traffic is safe here.";
  public static final String MESSAGE_OMITTING_FORMAT = "Omitting \"%s\" enables clear-text traffic. Make sure it is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::MSK::Cluster")) {
      checkMskCluster(ctx, resource);
    } else if (resource.isType("AWS::OpenSearchService::Domain") || resource.isType("AWS::Elasticsearch::Domain")) {
      checkSearchDomain(ctx, resource);
    } else if (resource.isType("AWS::ElasticLoadBalancingV2::Listener")) {
      checkLoadBalancingListener(ctx, resource);
    } else if (resource.isType("AWS::ECS::TaskDefinition")) {
      checkEcsTaskDefinition(ctx, resource);
    } else if (resource.isType("AWS::ElastiCache::ReplicationGroup")) {
      checkESReplicationGroup(ctx, resource);
    } else if (resource.isType("AWS::Kinesis::Stream")) {
      checkKinesisStream(ctx, resource);
    }
  }

  private static void checkMskCluster(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "EncryptionInfo", MappingTree.class)
      .flatMap(e -> PropertyUtils.value(e, "EncryptionInTransit", MappingTree.class))
      .ifPresent(e -> {
        checkClientBroker(ctx, e);
        reportOnFalseProperty(ctx, e, "InCluster", MESSAGE_CLEAR_TEXT);
      });
  }

  private static void checkClientBroker(CheckContext ctx, MappingTree e) {
    PropertyUtils.value(e, "ClientBroker", ScalarTree.class)
      .filter(clientBroker -> !"TLS".equals(clientBroker.value()))
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, MESSAGE_CLEAR_TEXT));
  }

  private static void checkSearchDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "NodeToNodeEncryptionOptions")
      .ifPresentOrElse(v -> reportOnFalseProperty(ctx, v, "Enabled", MESSAGE_CLEAR_TEXT),
        () -> reportResource(ctx, resource, omittingMessage("NodeToNodeEncryptionOptions")));

    PropertyUtils.get(resource.properties(), "DomainEndpointOptions")
      .ifPresentOrElse(v -> checkDomainEnforceHttp(ctx, v),
        () -> reportResource(ctx, resource, omittingMessage("DomainEndpointOptions")));
  }

  private static void checkDomainEnforceHttp(CheckContext ctx, PropertyTree domainEndpointOptions) {
    String enforceHTTPSKey = "EnforceHTTPS";
    if (PropertyUtils.isMissing(domainEndpointOptions.value(), enforceHTTPSKey)) {
      ctx.reportIssue(domainEndpointOptions.key(), omittingMessage(enforceHTTPSKey));
    }

    reportOnFalseProperty(ctx, domainEndpointOptions.value(), enforceHTTPSKey, MESSAGE_CLEAR_TEXT);
  }

  private static void checkLoadBalancingListener(CheckContext ctx, Resource resource) {
    Optional<Tree> rootProtocol = PropertyUtils.value(resource.properties(), "Protocol");
    if (rootProtocol.isEmpty() || !TextUtils.isValue(rootProtocol.get(), "HTTP").isTrue()) {
      return;
    }

    Optional<SequenceTree> defaultActions = PropertyUtils.value(resource.properties(), "DefaultActions", SequenceTree.class);
    if (defaultActions.isEmpty()) {
      return;
    }

    if (defaultActions.get().elements().stream().anyMatch(a -> isFixedResponseOrForwardAction(a) || isRedirectToHttpAction(a))) {
      ctx.reportIssue(rootProtocol.get(), MESSAGE_CLEAR_TEXT);
    }
  }

  private static boolean isFixedResponseOrForwardAction(YamlTree action) {
    Tree type = PropertyUtils.valueOrNull(action, "Type");
    return TextUtils.isValue(type, "fixed-response").isTrue() || TextUtils.isValue(type, "forward").isTrue();
  }

  private static boolean isRedirectToHttpAction(YamlTree action) {
    return TextUtils.isValue(PropertyUtils.valueOrNull(action, "Type"), "redirect").isTrue() &&
      TextUtils.isValue(XPathUtils.getSingleTree(action, "/RedirectConfig/Protocol").orElse(null), "HTTP").isTrue();
  }

  private static void checkEcsTaskDefinition(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "Volumes", SequenceTree.class)
      .ifPresent(volumes -> volumes.elements().forEach(v -> checkEcsTaskDefinitionVolume(ctx, v)));
  }

  private static void checkEcsTaskDefinitionVolume(CheckContext ctx, YamlTree volume) {
    Optional<PropertyTree> configuration = PropertyUtils.get(volume, "EFSVolumeConfiguration");
    if (configuration.isEmpty()) {
      return;
    }

    Optional<Tree> transitEncryption = PropertyUtils.value(configuration.get().value(), "TransitEncryption");
    if (transitEncryption.isPresent() && TextUtils.isValue(transitEncryption.get(), "DISABLED").isTrue()) {
      ctx.reportIssue(transitEncryption.get(), MESSAGE_CLEAR_TEXT);
    } else if (transitEncryption.isEmpty()) {
      ctx.reportIssue(configuration.get().key(), omittingMessage("TransitEncryption"));
    }
  }

  private static void checkESReplicationGroup(CheckContext ctx, Resource resource) {
    String encryptionPropertyKey = "TransitEncryptionEnabled";
    if (PropertyUtils.isMissing(resource.properties(), encryptionPropertyKey)) {
      reportResource(ctx, resource, omittingMessage(encryptionPropertyKey));
    } else {
      reportOnFalseProperty(ctx, resource.properties(), encryptionPropertyKey, MESSAGE_CLEAR_TEXT);
    }
  }

  private static void checkKinesisStream(CheckContext ctx, Resource resource) {
    if (PropertyUtils.isMissing(resource.properties(), "StreamEncryption")) {
      reportResource(ctx, resource, omittingMessage("StreamEncryption"));
    }
  }

  private static void reportOnFalseProperty(CheckContext ctx, @Nullable Tree tree, String propertyName, String message) {
    PropertyUtils.value(tree, propertyName, ScalarTree.class)
      .filter(TextUtils::isValueFalse)
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, message));
  }

  private static String omittingMessage(String domainEndpointOptions) {
    return String.format(MESSAGE_OMITTING_FORMAT, domainEndpointOptions);
  }
}
