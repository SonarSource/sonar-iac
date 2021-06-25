/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6258")
public class DisabledS3ServerAccessLoggingCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure disabling S3 server access logs is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (isS3BucketResource(block) && !StatementUtils.hasAttribute(block, "acl") && !StatementUtils.hasBlock(block, "logging")) {
      ctx.reportIssue(block.labels().get(0), MESSAGE);
    }
  }
}
