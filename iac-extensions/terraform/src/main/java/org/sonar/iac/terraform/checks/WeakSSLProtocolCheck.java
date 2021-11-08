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
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Change this code to use a stronger protocol.";
  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";
  private static final String ELASTIC_STRONG_POLICY = "Policy-Min-TLS-1-2-2019-07";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_api_gateway_domain_name")) {
      PropertyUtils.value(resource, "security_policy")
        .ifPresentOrElse(policy -> checkSecurityPolicy(ctx, policy),
          () -> reportResource(ctx, resource, MESSAGE));
    } else if (isResource(resource, "aws_apigatewayv2_domain_name")) {
      PropertyUtils.get(resource, "domain_name_configuration", BlockTree.class)
        .ifPresentOrElse(config -> checkDomainNameConfiguration(ctx, config),
          () -> reportResource(ctx, resource, MESSAGE));
    } else if (isResource(resource, "aws_elasticsearch_domain")) {
      PropertyUtils.get(resource, "domain_endpoint_options", BlockTree.class)
        .ifPresentOrElse(options -> checkDomainEndpointOptions(ctx, options),
          () -> reportResource(ctx, resource, MESSAGE));
    }
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, BlockTree config) {
    PropertyUtils.value(config, "security_policy")
      .ifPresentOrElse(policy -> checkSecurityPolicy(ctx, policy),
        () -> ctx.reportIssue(config.key(), MESSAGE));
  }

  private static void checkSecurityPolicy(CheckContext ctx, Tree policy) {
    if (TextUtils.isValue(policy, STRONG_SSL_PROTOCOL).isFalse()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }

  private static void checkDomainEndpointOptions(CheckContext ctx, BlockTree options) {
    PropertyUtils.value(options, "tls_security_policy")
      .ifPresentOrElse(policy -> checkElasticPolicy(ctx, policy),
        () -> ctx.reportIssue(options.key(), MESSAGE));
  }

  private static void checkElasticPolicy(CheckContext ctx, Tree policy) {
    if (TextUtils.isValue(policy, ELASTIC_STRONG_POLICY).isFalse()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }
}
