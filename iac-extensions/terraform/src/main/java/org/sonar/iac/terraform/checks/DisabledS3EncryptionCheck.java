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
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.api.tree.Tree;
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
        if (isSetEncryptionBlock(bodyStatement)) {
          return;
        }
      }
    }
    ctx.reportIssue(tree.labels().get(0), MESSAGE);
  }

  private static boolean isSetEncryptionBlock(Tree tree) {
    return tree instanceof BlockTree && "server_side_encryption_configuration".equals(((BlockTree) tree).type().value());
  }

  private static boolean isS3Bucket(BlockTree tree) {
    return !tree.labels().isEmpty() && "\"aws_s3_bucket\"".equals(tree.labels().get(0).value());
  }
}
