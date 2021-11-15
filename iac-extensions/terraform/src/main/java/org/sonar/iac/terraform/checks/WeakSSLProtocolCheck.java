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

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractMultipleResourcesCheck {

  private static final String MESSAGE = "Change this configuration to use a stronger protocol.";
  private static final String MESSAGE_OMITTING = "Omitting %s disables traffic encryption. Make sure it is safe here.";
  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_STRONG_POLICY = "Policy-Min-TLS-1-2-2019-07";
  public static final String SECURITY_POLICY = "security_policy";

  @Override
  void registerChecks() {
    register(WeakSSLProtocolCheck::checkApiGatewayDomainName, "aws_api_gateway_domain_name");
    register(WeakSSLProtocolCheck::checkApiGatewayV2DomainName, "aws_apigatewayv2_domain_name");
    register(WeakSSLProtocolCheck::checkElasticsearchDomain, "aws_elasticsearch_domain");
  }

  private static void checkApiGatewayDomainName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, SECURITY_POLICY, AttributeTree.class).ifPresentOrElse(policy ->
        reportUnexpectedValue(ctx, policy, STRONG_SSL_PROTOCOL, MESSAGE),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, SECURITY_POLICY)));
  }

  private static void checkApiGatewayV2DomainName(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_name_configuration", BlockTree.class).ifPresentOrElse(config ->
        checkDomainNameConfiguration(ctx, config),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, "domain_name_configuration.security_policy")));
  }

  private static void checkElasticsearchDomain(CheckContext ctx, BlockTree resource) {
    PropertyUtils.get(resource, "domain_endpoint_options", BlockTree.class).ifPresentOrElse(options ->
        checkDomainEndpointOptions(ctx, options),
      () -> reportResource(ctx, resource, String.format(MESSAGE_OMITTING, "domain_endpoint_options.tls_security_policy")));
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, BlockTree config) {
    PropertyUtils.value(config, SECURITY_POLICY).ifPresentOrElse(policy ->
        reportUnexpectedValue(ctx, policy, STRONG_SSL_PROTOCOL, MESSAGE),
      () -> ctx.reportIssue(config.key(), String.format(MESSAGE_OMITTING, SECURITY_POLICY)));
  }

  private static void checkDomainEndpointOptions(CheckContext ctx, BlockTree options) {
    PropertyUtils.get(options, "tls_security_policy", AttributeTree.class).ifPresentOrElse(policy ->
        reportUnexpectedValue(ctx, policy, ELASTIC_STRONG_POLICY, MESSAGE),
      () -> ctx.reportIssue(options.key(), String.format(MESSAGE_OMITTING, "tls_security_policy")));
  }
}
