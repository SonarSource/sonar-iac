/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.terraform.api.tree.BlockTree;

public abstract class AbstractResourceCheck implements IacCheck {

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isS3Bucket(tree)) {
        checkS3Bucket(ctx, tree);
      }
    });
  }

  // This method can be set later as non-abstract and a further checkResource method can be implemented.
  // This also allows checks without S3 bucket access that utils can use.
  protected abstract void checkS3Bucket(CheckContext ctx, BlockTree tree);

  public static boolean isResource(BlockTree tree) {
    return "resource".equals(tree.type().value());
  }

  public static boolean isS3Bucket(BlockTree tree) {
    return isResource(tree)
      && !tree.labels().isEmpty()
      && "\"aws_s3_bucket\"".equals(tree.labels().get(0).value());
  }
}
