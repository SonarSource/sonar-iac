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
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void registerChecks() {
    register(BucketsAccessCheck::checkBucket, S3_BUCKET);
  }

  private static void checkBucket(CheckContext ctx, BlockTree tree) {
    Optional<LiteralExprTree> acl = PropertyUtils.value(tree, "acl", TerraformTree.class)
      .filter(x -> x.is(TerraformTree.Kind.STRING_LITERAL))
      .map(LiteralExprTree.class::cast);

    if (acl.isPresent()) {
      LabelTree resourceType = tree.labels().get(0);
      String aclValue = acl.get().value();
      if ("public-read-write".equals(aclValue) || "public-read".equals(aclValue)) {
        ctx.reportIssue(acl.get(), String.format(MESSAGE, "AllUsers"), new SecondaryLocation(resourceType, SECONDARY_MSG));
      } else if ("authenticated-read".equals(aclValue)) {
        ctx.reportIssue(acl.get(), String.format(MESSAGE, "AuthenticatedUsers"), new SecondaryLocation(resourceType, SECONDARY_MSG));
      }
    }
  }
}
