/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.cloudformation.checks.utils.MappingTreeUtils;
import org.sonar.iac.common.api.checks.CheckContext;

@Rule(key = "S6295")
public class LogGroupRetentionPolicyCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure missing \"RetentionInDays\" property is intended here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::Logs::LogGroup")
      && !MappingTreeUtils.hasValue(resource.properties(), "RetentionInDays").isTrue()) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }
}
