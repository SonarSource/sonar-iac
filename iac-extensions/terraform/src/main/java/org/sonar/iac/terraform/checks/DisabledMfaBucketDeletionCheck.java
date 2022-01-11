/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2022 SonarSource SA
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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.ExpressionTree;
import org.sonar.iac.terraform.api.tree.LabelTree;

@Rule(key = "S6255")
public class DisabledMfaBucketDeletionCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure allowing object deletion of a S3 versioned bucket without MFA is safe here.";
  private static final String MESSAGE_SECONDARY = "Related bucket";

  @Override
  protected void registerResourceChecks() {
    register(DisabledMfaBucketDeletionCheck::checkBucket, S3_BUCKET);
  }

  private static void checkBucket(CheckContext ctx, BlockTree tree) {
    LabelTree resourceType = tree.labels().get(0);
    Optional<BlockTree> versioning = PropertyUtils.get(tree, "versioning", BlockTree.class);
    if (versioning.isPresent()) {
      Optional<AttributeTree> mfaDeleteAttribute = PropertyUtils.get(versioning.get(), "mfa_delete", AttributeTree.class);
      if (mfaDeleteAttribute.isPresent()) {
        ExpressionTree value = mfaDeleteAttribute.get().value();
        if (TextUtils.isValueFalse(value)) {
          ctx.reportIssue(mfaDeleteAttribute.get(), MESSAGE, new SecondaryLocation(resourceType, MESSAGE_SECONDARY));
        }
        return;
      }
      ctx.reportIssue(versioning.get().key(), MESSAGE, new SecondaryLocation(resourceType, MESSAGE_SECONDARY));
    }
  }
}
