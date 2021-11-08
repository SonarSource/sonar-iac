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

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.TupleTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S4423")
public class WeakSSLProtocolCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Change this code to use a stronger protocol.";
  private static final String STRONG_SSL_PROTOCOL = "TLS_1_2";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::ApiGateway::DomainName")) {
      PropertyUtils.value(resource.properties(), "SecurityPolicy")
        .ifPresentOrElse(policy -> checkSecurityPolicy(ctx, policy), () -> reportResource(ctx, resource, MESSAGE));
    } else if (resource.isType("AWS::ApiGatewayV2::DomainName")) {
      PropertyUtils.get(resource.properties(), "DomainNameConfigurations", TupleTree.class)
        .ifPresentOrElse(policy -> checkDomainNameConfiguration(ctx, policy), () -> reportResource(ctx, resource, MESSAGE));
    }
  }

  private static void checkDomainNameConfiguration(CheckContext ctx, TupleTree config) {
    PropertyUtils.value(config.value(), "SecurityPolicy")
      .ifPresentOrElse(policy -> checkSecurityPolicy(ctx, policy), () -> ctx.reportIssue(config.key(), MESSAGE));
  }

  private static void checkSecurityPolicy(CheckContext ctx, Tree policy) {
    if (TextUtils.isValue(policy, STRONG_SSL_PROTOCOL).isFalse()) {
      ctx.reportIssue(policy, MESSAGE);
    }
  }
}
