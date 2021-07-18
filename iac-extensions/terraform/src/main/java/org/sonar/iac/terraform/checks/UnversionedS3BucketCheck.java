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
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.ObjectElementTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.api.tree.TerraformTree.Kind;

@Rule(key = "S6252")
public class UnversionedS3BucketCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure using %s S3 bucket is safe here.";
  private static final String UNVERSIONED_MSG = "unversioned";
  private static final String SUSPENDED_MSG = "suspended versioned";
  private static final String SECONDARY_MESSAGE = "Related bucket";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree block) {
    if (!isS3Bucket(block)) {
      return;
    }
    LabelTree bucketLabel = block.labels().get(0);

    Optional<BlockTree> versioningBlock = PropertyUtils.get(block, "versioning", BlockTree.class);
    versioningBlock.ifPresent(b -> checkBlock(ctx, bucketLabel, b));

    Optional<AttributeTree> versioningAttribute = PropertyUtils.get(block, "versioning", AttributeTree.class);
    versioningAttribute.ifPresent(a -> checkAttribute(ctx, bucketLabel, a));

    if (!versioningBlock.isPresent() && !versioningAttribute.isPresent()) {
      ctx.reportIssue(bucketLabel, String.format(MESSAGE, UNVERSIONED_MSG));
    }
  }

  private static void checkBlock(CheckContext ctx, LabelTree bucket, BlockTree block) {
    Optional<AttributeTree> enabled = PropertyUtils.get(block, "enabled", AttributeTree.class);
    if (enabled.isPresent()) {
      checkSuspendedVersioning(ctx, bucket, enabled.get(), enabled.get().value());
    } else {
      ctx.reportIssue(block.key(), String.format(MESSAGE, UNVERSIONED_MSG), new SecondaryLocation(bucket, SECONDARY_MESSAGE));
    }
  }

  private static void checkAttribute(CheckContext ctx, LabelTree bucketLabel, AttributeTree attribute) {
    if (attribute.value().is(Kind.OBJECT)) {
      Optional<ObjectElementTree> enabled = PropertyUtils.get(attribute.value(), "enabled", ObjectElementTree.class);
      if (enabled.isPresent()) {
        checkSuspendedVersioning(ctx, bucketLabel, enabled.get(), enabled.get().value());
      } else {
        ctx.reportIssue(attribute.key(), String.format(MESSAGE, UNVERSIONED_MSG), new SecondaryLocation(bucketLabel, SECONDARY_MESSAGE));
      }
    }
  }

  private static void checkSuspendedVersioning(CheckContext ctx, LabelTree bucket, TerraformTree setting, ExpressionTree value) {
    if (TextUtils.isValueFalse(value)) {
      ctx.reportIssue(setting, String.format(MESSAGE, SUSPENDED_MSG), new SecondaryLocation(bucket, SECONDARY_MESSAGE));
    }
  }
}
