/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.utils.LiteralUtils;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6255")
public class DisabledMfaBucketDeletionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.";
  private static final String MESSAGE_SECONDARY = "Should be true";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree tree) {
    if (!isS3Bucket(tree)) {
      return;
    }

    Optional<BlockTree> versioning = StatementUtils.getBlock(tree, "versioning");
    if (versioning.isPresent()) {
      Optional<AttributeTree> mfaDeleteAttribute = StatementUtils.getAttribute(versioning.get(), "mfa_delete");
      if (!mfaDeleteAttribute.isPresent()) {
        ctx.reportIssue(tree.labels().get(0), MESSAGE);
      } else if (LiteralUtils.isFalse(mfaDeleteAttribute.get().value())) {
        ctx.reportIssue(tree.labels().get(0), MESSAGE, new SecondaryLocation(mfaDeleteAttribute.get(), MESSAGE_SECONDARY));
      }
    } else {
      ctx.reportIssue(tree.labels().get(0), MESSAGE);
    }
  }
}
