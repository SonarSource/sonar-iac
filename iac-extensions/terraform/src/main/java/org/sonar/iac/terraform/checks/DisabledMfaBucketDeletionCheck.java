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
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6255")
public class DisabledMfaBucketDeletionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.";
  private static final String MESSAGE_SECONDARY = "Related bucket";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree tree) {
    if (!isS3Bucket(tree)) {
      return;
    }


    LabelTree resourceType = tree.labels().get(0);
    Optional<BlockTree> versioning = StatementUtils.getBlock(tree, "versioning");
    if (versioning.isPresent()) {
      Optional<AttributeTree> mfaDeleteAttribute = StatementUtils.getAttribute(versioning.get(), "mfa_delete");
      if (mfaDeleteAttribute.isPresent()) {
        ExpressionTree value = mfaDeleteAttribute.get().value();
        if (TextUtils.isValueFalse(value)) {
          ctx.reportIssue(mfaDeleteAttribute.get(), MESSAGE, new SecondaryLocation(resourceType, MESSAGE_SECONDARY));
        }
        return;
      }
      ctx.reportIssue(versioning.get().identifier(), MESSAGE, new SecondaryLocation(resourceType, MESSAGE_SECONDARY));
      return;
    }
    ctx.reportIssue(resourceType, MESSAGE);
  }
}
