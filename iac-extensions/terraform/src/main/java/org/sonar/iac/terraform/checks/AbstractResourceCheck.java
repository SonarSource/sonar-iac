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

import java.util.List;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.IacCheck;
import org.sonar.iac.common.api.checks.InitContext;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;

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

  public static void reportIssueOnResource(CheckContext ctx, BlockTree resource, String message) {
    List<LabelTree> labels = resource.labels();
    ctx.reportIssue(labels.isEmpty() ? resource.key() : labels.get(0), message);
  }
}
