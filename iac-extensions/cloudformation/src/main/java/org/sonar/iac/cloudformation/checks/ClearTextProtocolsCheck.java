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
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractResourceCheck {

  private static final String MESSAGE_BROKER_FORMAT = "Using %s protocol is insecure. Use TLS instead";
  private static final String MESSAGE_CLUSTER = "Communication among nodes of a cluster should be encrypted";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::MSK::Cluster")) {
      return;
    }

    PropertyUtils.value(resource.properties(), "EncryptionInfo", MappingTree.class)
      .flatMap(e -> PropertyUtils.value(e, "EncryptionInTransit", MappingTree.class))
      .ifPresent(e -> {
        checkClientBroker(ctx, e);
        checkInCluster(ctx, e);
      });
  }

  private static void checkClientBroker(CheckContext ctx, MappingTree e) {
    PropertyUtils.value(e, "ClientBroker", ScalarTree.class)
      .filter(clientBroker -> !"TLS".equals(clientBroker.value()))
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, String.format(MESSAGE_BROKER_FORMAT, clientBroker.value())));
  }

  private void checkInCluster(CheckContext ctx, MappingTree e) {
    PropertyUtils.value(e, "InCluster", ScalarTree.class)
      .filter(TextUtils::isValueFalse)
      .ifPresent(clientBroker -> ctx.reportIssue(clientBroker, MESSAGE_CLUSTER));
  }
}
