/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.checks.utils.Policy;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isResource(tree)) {
        checkResource(ctx, tree);
      }
    });
  }

  protected abstract void checkResource(CheckContext ctx, BlockTree resource);

  public static boolean isResource(BlockTree tree) {
    return TextUtils.isValue(tree.key(), "resource").isTrue();
  }

  public static boolean isResource(BlockTree tree, String type) {
    return isResource(tree) && !tree.labels().isEmpty() && type.equals(tree.labels().get(0).value());
  }

  public static boolean isS3Bucket(BlockTree tree) {
    return !tree.labels().isEmpty() && "aws_s3_bucket".equals(tree.labels().get(0).value());
  }

  public static boolean isS3BucketResource(BlockTree tree) {
    return isResource(tree, "aws_s3_bucket");
  }

  public static Optional<Policy> policy(BlockTree resource) {
    return PropertyUtils
      .value(resource, key -> key.contains("policy"))
      .map(Policy::from);
  }
}
