/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.terraform.api.tree.AttributeTree;
import org.sonar.iac.terraform.api.tree.BlockTree;
import org.sonar.iac.terraform.api.tree.LiteralExprTree;
import org.sonar.iac.terraform.api.tree.TerraformTree;
import org.sonar.iac.terraform.checks.utils.StatementUtils;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree tree) {
    if (!isS3Bucket(tree)) {
      return;
    }

    Optional<String> acl = StatementUtils.getAttribute(tree, "acl")
      .map(AttributeTree::value)
      .filter(x -> x.is(TerraformTree.Kind.STRING_LITERAL))
      .map(x -> ((LiteralExprTree) x).value());

    if (acl.isPresent()) {
      if ("public-read-write".equals(acl.get()) || "public-read".equals(acl.get())) {
        ctx.reportIssue(tree.labels().get(0), String.format(MESSAGE, "AllUsers"));
      } else if ("authenticated-read".equals(acl.get())) {
        ctx.reportIssue(tree.labels().get(0), String.format(MESSAGE, "AuthenticatedUsers"));
      }
    }
  }
}
