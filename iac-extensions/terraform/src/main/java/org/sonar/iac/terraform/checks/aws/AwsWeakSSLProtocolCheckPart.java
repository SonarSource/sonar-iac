/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
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
package org.sonar.iac.terraform.checks.aws;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.AbstractResourceCheck;

import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.OMITTING_WEAK_SSL_MESSAGE;
import static org.sonar.iac.terraform.checks.WeakSSLProtocolCheck.WEAK_SSL_MESSAGE;

public class AwsWeakSSLProtocolCheckPart extends AbstractResourceCheck {

  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_WEAK_POLICY = "Policy-Min-TLS-1-0-2019-07";
  private static final String SECURITY_POLICY = "security_policy";

  @Override
  protected void registerResourceChecks() {
    register(AwsWeakSSLProtocolCheckPart::checkApiGatewayDomainName, "aws_api_gateway_domain_name");
    register(AwsWeakSSLProtocolCheckPart::checkApiGatewayV2DomainName, "aws_apigatewayv2_domain_name");
    register(AwsWeakSSLProtocolCheckPart::checkOpenSearchDomain, "aws_elasticsearch_domain", "aws_opensearch_domain");
  }

  private static void checkApiGatewayDomainName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, SECURITY_POLICY, AttributeTree.class)
      .ifPresentOrElse(policy -> reportUnexpectedValue(ctx, policy, STRONG_SSL_PROTOCOL, WEAK_SSL_MESSAGE),
        () -> reportResource(ctx, resource, String.format(OMITTING_WEAK_SSL_MESSAGE, SECURITY_POLICY)));
  }

  private static void checkApiGatewayV2DomainName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_name_configuration", BlockTree.class).ifPresentOrElse(config -> checkDomainNameConfiguration(ctx, config),
      () -> reportResource(ctx, resource, String.format(OMITTING_WEAK_SSL_MESSAGE, "domain_name_configuration.security_policy")));
  }

  private static void checkOpenSearchDomain(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_endpoint_options", BlockTree.class)
      .ifPresentOrElse(options -> checkDomainEndpointOptions(ctx, options),
        () -> reportResource(ctx, resource, String.format(OMITTING_WEAK_SSL_MESSAGE, "domain_endpoint_options.tls_security_policy")));
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, BlockTree config) {
    PropertyUtils.get(config, SECURITY_POLICY, AttributeTree.class)
      .ifPresentOrElse(policy -> reportUnexpectedValue(ctx, policy, STRONG_SSL_PROTOCOL, WEAK_SSL_MESSAGE),
        () -> ctx.reportIssue(config.key(), String.format(OMITTING_WEAK_SSL_MESSAGE, SECURITY_POLICY)));
  }

  private static void checkDomainEndpointOptions(CheckContext ctx, BlockTree options) {
    PropertyUtils.get(options, "tls_security_policy", AttributeTree.class)
      .ifPresentOrElse(policy -> reportSensitiveValue(ctx, policy, ELASTIC_WEAK_POLICY, WEAK_SSL_MESSAGE),
        () -> ctx.reportIssue(options.key(), String.format(OMITTING_WEAK_SSL_MESSAGE, "tls_security_policy")));
  }
}
