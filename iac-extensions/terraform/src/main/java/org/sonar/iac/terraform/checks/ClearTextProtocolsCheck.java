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
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;

@Rule(key = "S5332")
public class ClearTextProtocolsCheck extends AbstractResourceCheck {

  private static final String MESSAGE_BROKER_FORMAT = "Using %s protocol is insecure. Use TLS instead";
  private static final String MESSAGE_CLUSTER = "Communication among nodes of a cluster should be encrypted";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_msk_cluster")) {
      return;
    }

    PropertyUtils.get(resource, "encryption_info", BlockTree.class)
      .flatMap(e -> PropertyUtils.get(e, "encryption_in_transit", BlockTree.class))
      .ifPresent(e -> {
        checkClientBroker(ctx, e);
        checkInCluster(ctx, e);
      });
  }

  private void checkClientBroker(CheckContext ctx, BlockTree encryptionBlock) {
    PropertyUtils.value(encryptionBlock, "client_broker", LiteralExprTree.class)
      .ifPresent(clientBroker -> {
        if (!"TLS".equals(clientBroker.value())) {
          ctx.reportIssue(clientBroker, String.format(MESSAGE_BROKER_FORMAT, clientBroker.value()));
        }
      });
  }

  private void checkInCluster(CheckContext ctx, BlockTree encryptionBlock) {
    PropertyUtils.value(encryptionBlock, "in_cluster", LiteralExprTree.class)
      .ifPresent(inCluster -> {
        if (TextUtils.isValueFalse(inCluster)) {
          ctx.reportIssue(inCluster, MESSAGE_CLUSTER);
        }
      });
  }
}
