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
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LabelTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree tree) {
    if (!isS3Bucket(tree)) {
      return;
    }

    Optional<LiteralExprTree> acl = StatementUtils.getAttribute(tree, "acl")
      .map(AttributeTree::value)
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
