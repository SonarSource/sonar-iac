/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2026 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.ScalarTree;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;
import org.sonar.iac.common.yaml.tree.YamlTree;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.sonar.iac.common.checks.PropertyUtils.hasValueEqual;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Change this code to disable support of older TLS versions.";
  private static final String MESSAGE_OMITTING_FORMAT = "Set \"%s\" to disable support of older TLS versions.";
  private static final String MESSAGE_NO_COMPLIANT_POLICY = "Add a policy of type \"SSLNegotiationPolicyType\" to disable support of " +
    "older TLS versions";

  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_WEAK_POLICY = "Policy-Min-TLS-1-0-2019-07";
  private static final String SECURITY_POLICY_KEY = "SecurityPolicy";
  private static final String SSL_POLICY_KEY = "SslPolicy";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    var type = resource.typeAsString();
    switch (type) {
      case "AWS::ApiGateway::DomainName" -> checkApiGatewayDomain(ctx, resource);
      case "AWS::ApiGatewayV2::DomainName" -> checkApiGatewayDomainV2(ctx, resource);
      case "AWS::Elasticsearch::Domain", "AWS::OpenSearchService::Domain" -> checkSearchDomain(ctx, resource);
      case "AWS::ElasticLoadBalancingV2::Listener" -> checkElbv2Listener(ctx, resource);
      case "AWS::ElasticLoadBalancing::LoadBalancer" -> checkClassicElb(ctx, resource);
    }
  }

  private static void checkApiGatewayDomainV2(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "DomainNameConfigurations", TupleTree.class)
      .ifPresent(policy -> checkDomainNameConfiguration(ctx, policy));
  }

  private static void checkApiGatewayDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), SECURITY_POLICY_KEY)
      .ifPresent(policy -> checkSecurityPolicy(ctx, policy));
  }

  private static void checkSearchDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "DomainEndpointOptions", TupleTree.class)
      .ifPresentOrElse(options -> checkDomainEndpointOptions(ctx, options),
        () -> reportResource(ctx, resource, omittingMessage("DomainEndpointOptions.TLSSecurityPolicy")));
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, TupleTree config) {
    if (config.value() instanceof SequenceTree sequenceTree && configSequenceContainsSecurityPolicy(sequenceTree)) {
      getSecurityPolicyFromConfigSequence(sequenceTree)
        .ifPresent(policy -> checkSecurityPolicy(ctx, policy));
    }
  }

  /**
   * Check if in the sequence is a MappingTree which contains the SecurityPolicy key,
   * or an unknown element which could represent the SecurityPolicy
   */
  private static boolean configSequenceContainsSecurityPolicy(SequenceTree sequenceTree) {
    return sequenceTree.elements().stream().anyMatch(map -> PropertyUtils.has(map, SECURITY_POLICY_KEY).isTrue());
  }

  private static Optional<Tree> getSecurityPolicyFromConfigSequence(SequenceTree sequenceTree) {
    return sequenceTree.elements().stream()
      .map(map -> PropertyUtils.value(map, SECURITY_POLICY_KEY))
      .flatMap(Optional::stream)
      .findFirst();
  }

  private static void checkSecurityPolicy(CheckContext ctx, Tree policy) {
    if (TextUtils.isValue(policy, STRONG_SSL_PROTOCOL).isFalse()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }

  private static void checkDomainEndpointOptions(CheckContext ctx, TupleTree options) {
    PropertyUtils.value(options.value(), "TLSSecurityPolicy")
      .ifPresentOrElse(policy -> checkElasticPolicy(ctx, policy),
        () -> ctx.reportIssue(options.key(), omittingMessage("TLSSecurityPolicy")));
  }

  private static void checkElasticPolicy(CheckContext ctx, Tree policy) {
    if (TextUtils.isValue(policy, ELASTIC_WEAK_POLICY).isTrue()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }

  private static String omittingMessage(String missingProperty) {
    return String.format(MESSAGE_OMITTING_FORMAT, missingProperty);
  }

  private static void checkElbv2Listener(CheckContext ctx, Resource resource) {
    if (!hasSecureProtocol(resource.properties())) {
      return;
    }

    PropertyUtils.value(resource.properties(), SSL_POLICY_KEY)
      .ifPresentOrElse(policy -> checkElbSslPolicy(ctx, policy),
        () -> reportResource(ctx, resource, omittingMessage(SSL_POLICY_KEY)));
  }

  private static boolean hasSecureProtocol(@Nullable YamlTree tree) {
    return Optional.ofNullable(tree)
      .flatMap(t -> PropertyUtils.value(tree, "Protocol"))
      .flatMap(TextUtils::getValue)
      .filter(it -> "HTTPS".equalsIgnoreCase(it) || "TLS".equalsIgnoreCase(it) || "SSL".equalsIgnoreCase(it))
      .isPresent();
  }

  private static void checkClassicElb(CheckContext ctx, Resource resource) {
    var policies = PropertyUtils.get(resource.properties(), "Policies", TupleTree.class)
      .map(TupleTree::value)
      .filter(SequenceTree.class::isInstance)
      .map(SequenceTree.class::cast)
      .stream()
      .flatMap(it -> it.elements().stream())
      .filter(it -> PropertyUtils.has(it, "PolicyName").isTrue())
      .collect(toMap(
        it -> PropertyUtils.value(it, "PolicyName")
          .flatMap(TextUtils::getValue)
          .orElse(""),
        identity()));

    PropertyUtils.get(resource.properties(), "Listeners")
      .ifPresent(listenersProperty -> {
        if (listenersProperty.value() instanceof SequenceTree listeners) {
          for (YamlTree listener : listeners.elements()) {
            checkClassicElbListener(ctx, listener, listenersProperty, policies);
          }
        }
      });
  }

  private static void checkClassicElbListener(
    CheckContext ctx,
    YamlTree listener,
    PropertyTree listenersProperty,
    Map<String, YamlTree> policiesByName) {
    if (!hasSecureProtocol(listener)) {
      return;
    }

    var listenerPolicyNames = getListenerPolicyNames(listener);
    if (listenerPolicyNames.isEmpty()) {
      ctx.reportIssue(listenersProperty.key(), omittingMessage("PolicyNames"));
      return;
    }

    if (policiesByName.isEmpty()) {
      ctx.reportIssue(listenersProperty.key(), omittingMessage("Policies"));
      return;
    }

    boolean foundAnySslPolicy = false;
    for (var listenerPolicyName : listenerPolicyNames) {
      var policy = policiesByName.get(listenerPolicyName);
      if (policy != null && hasValueEqual(policy, "PolicyType", "SSLNegotiationPolicyType")) {
        foundAnySslPolicy = true;
        checkClassicElbPolicyAttributes(ctx, policy);
      }
    }

    if (!foundAnySslPolicy) {
      ctx.reportIssue(listenersProperty.key(), MESSAGE_NO_COMPLIANT_POLICY);
    }
  }

  private static List<String> getListenerPolicyNames(YamlTree listener) {
    return PropertyUtils.value(listener, "PolicyNames", SequenceTree.class)
      .map(policyNames -> policyNames.elements().stream()
        .filter(ScalarTree.class::isInstance)
        .map(ScalarTree.class::cast)
        .map(ScalarTree::value)
        .toList())
      .orElse(List.of());
  }

  private static void checkClassicElbPolicyAttributes(CheckContext ctx, YamlTree policy) {
    PropertyUtils.value(policy, "Attributes", SequenceTree.class)
      .ifPresent(attributes -> {
        for (YamlTree attribute : attributes.elements()) {
          if (hasValueEqual(attribute, "Name", "Reference-Security-Policy")) {
            PropertyUtils.value(attribute, "Value")
              .ifPresentOrElse(value -> checkElbSslPolicy(ctx, value),
                () -> ctx.reportIssue(attribute, omittingMessage("Value")));
            return;
          }
        }
        ctx.reportIssue(policy, omittingMessage("Reference-Security-Policy"));
      });
  }

  private static void checkElbSslPolicy(CheckContext ctx, Tree policy) {
    if (isWeakElbSslPolicy(policy)) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }

  private static boolean isWeakElbSslPolicy(Tree policy) {
    return TextUtils.matchesValue(policy, policyValue -> policyValue.contains("-1-0-") || policyValue.contains("-1-1-") ||
      "ELBSecurityPolicy-2016-08".equals(policyValue)).isTrue();
  }
}
