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
import org.sonar.iac.terraform.api.tree.ObjectTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;
import org.sonar.iac.terraform.checks.utils.ObjectUtils;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String UNVERSIONED_MSG = "unversioned";
  private static final String SUSPENDED_MSG = "suspended versioned";
  private static final String SUSPENDED_MSG_SECONDARY = "Suspended versioning.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (!isS3Bucket(block)) {
      return;
    }
    LabelTree bucketLabel = block.labels().get(0);

    Optional<BlockTree> versioningBlock = StatementUtils.getBlock(block, "versioning");
    if (versioningBlock.isPresent()) {
      Optional<AttributeTree> enabled = StatementUtils.getAttribute(versioningBlock.get(), "enabled");
      if (enabled.isPresent()) {
        checkSuspendedVersioning(ctx, bucketLabel, enabled.get().value());
        return;
      }
    }
    Optional<AttributeTree> versioningAttribute = StatementUtils.getAttribute(block, "versioning");
    if (versioningAttribute.isPresent()) {
      if (versioningAttribute.get().value().is(Kind.OBJECT)) {
        ObjectUtils.getElement((ObjectTree) versioningAttribute.get().value(), "enabled")
          .ifPresent(objectElementTree -> checkSuspendedVersioning(ctx, bucketLabel, objectElementTree.value()));
      }
      return;
    }
    ctx.reportIssue(bucketLabel, String.format(MESSAGE, UNVERSIONED_MSG));
  }

  private static void checkSuspendedVersioning(CheckContext ctx, LabelTree bucket, ExpressionTree setting) {
    if (TextUtils.isValueFalse(setting)) {
      ctx.reportIssue(bucket, String.format(MESSAGE, SUSPENDED_MSG), new SecondaryLocation(setting, SUSPENDED_MSG_SECONDARY));
    }
  }

}
