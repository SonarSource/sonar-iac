/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;

@Rule(key = "S6319")
public class UnencryptedSageMakerNotebookCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted SageMaker notebook instances is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::SageMaker::NotebookInstance")) {
      return;
    }
    if (PropertyUtils.has(resource.properties(), "KmsKeyId").isFalse()) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

}
