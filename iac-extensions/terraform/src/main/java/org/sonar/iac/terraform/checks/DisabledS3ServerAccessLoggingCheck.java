/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6258")
public class DisabledS3ServerAccessLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure disabling S3 server access logs is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (isS3BucketResource(block) && !isMaybeLoggingBucket(block) && !StatementUtils.hasBlock(block, "logging")) {
      ctx.reportIssue(block.labels().get(0), MESSAGE);
    }
  }

  private static boolean isMaybeLoggingBucket(BlockTree block) {
    Optional<AttributeTree> acl = StatementUtils.getAttribute(block, "acl");
    if (!acl.isPresent()) {
      return false;
    }
    ExpressionTree aclValue = acl.get().value();
    if (aclValue.is(Kind.STRING_LITERAL)) {
      return ((LiteralExprTree) aclValue).value().equals("log-delivery-write");
    }
    return true;
  }
}
