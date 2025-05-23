/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.common.yaml.tree.SequenceTree;
import org.sonar.iac.common.yaml.tree.TupleTree;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Change this code to disable support of older TLS versions.";
  private static final String MESSAGE_OMITTING_FORMAT = "Set \"%s\" to disable support of older TLS versions.";

  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_WEAK_POLICY = "Policy-Min-TLS-1-0-2019-07";
  private static final String SECURITY_POLICY_KEY = "SecurityPolicy";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::ApiGateway::DomainName")) {
      checkApiGatewayDomain(ctx, resource);
    } else if (resource.isType("AWS::ApiGatewayV2::DomainName")) {
      checkApiGatewayDomainV2(ctx, resource);
    } else if (resource.isType("AWS::Elasticsearch::Domain") || resource.isType("AWS::OpenSearchService::Domain")) {
      checkSearchDomain(ctx, resource);
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
}
