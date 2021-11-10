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

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

@Rule(key = "S6258")
public class DisabledLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that disabling logging is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (isS3BucketResource(block)) {
      checkS3Bucket(ctx, block);
    } else if (isResource(block, "aws_api_gateway_stage")) {
      checkApiGatewayStage(ctx, block);
    }
  }

  private static void checkS3Bucket(CheckContext ctx, BlockTree block) {
    if (!isMaybeLoggingBucket(block) && !PropertyUtils.has(block, "logging").isTrue()) {
      reportResource(ctx, block, MESSAGE);
    }
  }

  private static boolean isMaybeLoggingBucket(BlockTree block) {
    Optional<AttributeTree> acl = PropertyUtils.get(block, "acl", AttributeTree.class);
    if (acl.isEmpty()) {
      return false;
    }
    ExpressionTree aclValue = acl.get().value();
    if (aclValue.is(Kind.STRING_LITERAL)) {
      return ((LiteralExprTree) aclValue).value().equals("log-delivery-write");
    }
    return true;
  }

  private static void checkApiGatewayStage(CheckContext ctx, BlockTree block) {
    PropertyUtils.valueOrRun(block, "xray_tracing_enabled", () -> reportResource(ctx, block, MESSAGE))
      .filter(TextUtils::isValueFalse)
      .ifPresent(tracing -> ctx.reportIssue(tracing, MESSAGE));
  }
}
