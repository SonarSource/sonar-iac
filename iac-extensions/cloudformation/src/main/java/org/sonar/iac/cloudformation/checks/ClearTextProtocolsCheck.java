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
import org.sonar.iac.cloudformation.api.tree.MappingTree;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractResourceCheck {

  private static final String MESSAGE_PROTOCOL_FORMAT = "Using %s protocol is insecure. Use TLS instead";
  private static final String MESSAGE_CLEAR_TEXT = "Make sure allowing clear-text traffic is safe here";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::MSK::Cluster")) {
      checkMskCluster(ctx, resource);
    } else if (resource.isType("AWS::OpenSearchService::Domain") || resource.isType("AWS::Elasticsearch::Domain")) {
      checkSearchDomain(ctx, resource);
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
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, String.format(MESSAGE_PROTOCOL_FORMAT, clientBroker.value())));
  }

  private static void checkSearchDomain(CheckContext ctx, Resource resource) {
    PropertyUtils.value(resource.properties(), "NodeToNodeEncryptionOptions")
      .ifPresentOrElse(v -> reportOnFalseProperty(ctx, v, "Enabled", MESSAGE_CLEAR_TEXT),
        () -> reportResource(ctx, resource, MESSAGE_CLEAR_TEXT));

    PropertyUtils.get(resource.properties(), "DomainEndpointOptions")
      .ifPresentOrElse(v -> checkDomainEnforceHttp(ctx, v), () -> reportResource(ctx, resource, MESSAGE_CLEAR_TEXT));
  }

  private static void checkDomainEnforceHttp(CheckContext ctx, PropertyTree domainEndpointOptions) {
    if (PropertyUtils.has(domainEndpointOptions.value(), "EnforceHTTPS").isFalse()) {
      ctx.reportIssue(domainEndpointOptions.key(), MESSAGE_CLEAR_TEXT);
    }

    reportOnFalseProperty(ctx, domainEndpointOptions.value(), "EnforceHTTPS", MESSAGE_CLEAR_TEXT);
  }

  private static void reportOnFalseProperty(CheckContext ctx, Tree tree, String propertyName, String message) {
    PropertyUtils.value(tree, propertyName, ScalarTree.class)
      .filter(TextUtils::isValueFalse)
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, message));
  }
}
