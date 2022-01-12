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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.SequenceTree;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Change this configuration to use a stronger protocol.";
  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_STRONG_POLICY = "Policy-Min-TLS-1-2-2019-07";
  private static final String MESSAGE_OMITTING_FORMAT = "Omitting %s disables traffic encryption.";
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
      .ifPresentOrElse(policy -> checkDomainNameConfiguration(ctx, policy),
        () -> reportResource(ctx, resource, omittingMessage("DomainNameConfigurations.SecurityPolicy")));
  }

  private static void checkApiGatewayDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), SECURITY_POLICY_KEY)
      .ifPresentOrElse(policy -> checkSecurityPolicy(ctx, policy),
        () -> reportResource(ctx, resource, omittingMessage(SECURITY_POLICY_KEY)));
  }

  private static void checkSearchDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.get(resource.properties(), "DomainEndpointOptions", TupleTree.class)
      .ifPresentOrElse(options -> checkDomainEndpointOptions(ctx, options),
        () -> reportResource(ctx, resource, omittingMessage("DomainEndpointOptions.TLSSecurityPolicy")));
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, TupleTree config) {
    if (config.value() instanceof SequenceTree && configSequenceContainsSecurityPolicy((SequenceTree) config.value())) {
      getSecurityPolicyFromConfigSequence((SequenceTree) config.value())
        .ifPresent(policy -> checkSecurityPolicy(ctx, policy));
    } else {
      ctx.reportIssue(config.key(), omittingMessage(SECURITY_POLICY_KEY));
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
    if (TextUtils.isValue(policy, ELASTIC_STRONG_POLICY).isFalse()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }

  private static String omittingMessage(String missingProperty) {
    return String.format(MESSAGE_OMITTING_FORMAT, missingProperty);
  }
}
