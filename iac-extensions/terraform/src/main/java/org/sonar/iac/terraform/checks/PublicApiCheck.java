/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6333")
public class PublicApiCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure creating a public API is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_api_gateway_method")) {
      PropertyUtils.value(resource, "authorization")
        .filter(auth -> TextUtils.isValue(auth, "NONE").isTrue())
        .ifPresent(auth -> ctx.reportIssue(auth, MESSAGE, new SecondaryLocation(resource.labels().get(0), "Related method")));
    }
  }
}
