/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.BodyTree;

@Rule(key = "S6245")
public class DisabledS3EncryptionCheck implements IacCheck {
  private static final String MESSAGE = "Make sure not using server-side encryption is safe here.";

  @Override
  public void initialize(InitContext init) {
    init.register(BlockTree.class, (ctx, tree) -> {
      if (isS3Bucket(tree)) {
        checkBucket(ctx, tree);
      }
    });
  }

  private static void checkBucket(CheckContext ctx, BlockTree tree) {
    Optional<BodyTree> body = tree.body();
    if (body.isPresent()) {
      for (Tree bodyStatement : body.get().statements()) {
        if (isSetEncryption(bodyStatement)) {
          return;
        }
      }
    }
    ctx.reportIssue(tree.labels().get(0), MESSAGE);
  }

  private static boolean isSetEncryption(Tree tree) {
    return (tree instanceof BlockTree && "server_side_encryption_configuration".equals(((BlockTree) tree).type().value())) ||
      (tree instanceof AttributeTree && "server_side_encryption_configuration".equals(((AttributeTree) tree).name().value()));
  }

  private static boolean isS3Bucket(BlockTree tree) {
    return !tree.labels().isEmpty() && "\"aws_s3_bucket\"".equals(tree.labels().get(0).value());
  }
}
