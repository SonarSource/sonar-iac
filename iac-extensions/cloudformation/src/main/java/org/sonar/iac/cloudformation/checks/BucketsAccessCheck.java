/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.api.tree.ScalarTree;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!isS3Bucket(resource)) {
      return;
    }

    Optional<String> acl = MappingTreeUtils.getValue(resource.properties(), "AccessControl")
      .filter(ScalarTree.class::isInstance)
      .flatMap(TextUtils::getValue);

    if (acl.isPresent()) {
      if ("PublicReadWrite".equals(acl.get()) || "PublicRead".equals(acl.get())) {
        ctx.reportIssue(resource.type(), String.format(MESSAGE, "AllUsers"));
      } else if ("AuthenticatedRead".equals(acl.get())) {
        ctx.reportIssue(resource.type(), String.format(MESSAGE, "AuthenticatedUsers"));
      }
    }
  }
}
