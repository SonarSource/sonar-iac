/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2024 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6265")
public class BucketsAccessCheck extends AbstractResourceCheck {
  private static final String MESSAGE = "Make sure granting access to \"%s\" group is safe here.";
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
