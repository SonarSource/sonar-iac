/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6319")
public class UnencryptedSageMakerNotebookCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted SageMaker notebook instances is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_sagemaker_notebook_instance") && PropertyUtils.has(resource, "kms_key_id").isFalse()) {
      ctx.reportIssue(resource.labels().get(0), MESSAGE);
    }
  }

}
