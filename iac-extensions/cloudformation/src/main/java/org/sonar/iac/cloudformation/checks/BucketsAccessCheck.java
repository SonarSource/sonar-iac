/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to %s group is safe here.";
  private static final String SECONDARY_MSG = "Related bucket";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!isS3Bucket(resource)) {
      return;
    }

    PropertyUtils.value(resource.properties(), "AccessControl")
      .ifPresent(acl -> {
        String aclValue = TextUtils.getValue(acl).orElse(null);
        if ("PublicReadWrite".equals(aclValue) || "PublicRead".equals(aclValue)) {
          ctx.reportIssue(acl, String.format(MESSAGE, "AllUsers"), new SecondaryLocation(resource.type(), SECONDARY_MSG));
        } else if ("AuthenticatedRead".equals(aclValue)) {
          ctx.reportIssue(acl, String.format(MESSAGE, "AuthenticatedUsers"), new SecondaryLocation(resource.type(), SECONDARY_MSG));
        }
      });
  }
}
