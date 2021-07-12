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
import org.sonar.iac.common.api.checks.SecondaryLocation;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!isS3Bucket(resource)) {
      return;
    }

    Optional<ScalarTree> acl = MappingTreeUtils.getValue(resource.properties(), "AccessControl")
      .filter(ScalarTree.class::isInstance)
      .map(ScalarTree.class::cast);

    if (acl.isPresent()) {
      String aclValue = acl.get().value();
      if ("PublicReadWrite".equals(aclValue) || "PublicRead".equals(aclValue)) {
        ctx.reportIssue(acl.get(), String.format(MESSAGE, "AllUsers"), new SecondaryLocation(resource.type(), SECONDARY_MSG));
      } else if ("AuthenticatedRead".equals(aclValue)) {
        ctx.reportIssue(acl.get(), String.format(MESSAGE, "AuthenticatedUsers"), new SecondaryLocation(resource.type(), SECONDARY_MSG));
      }
    }
  }
}
