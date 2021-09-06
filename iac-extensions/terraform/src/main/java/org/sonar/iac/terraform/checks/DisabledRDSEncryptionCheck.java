/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6303")
public class DisabledRDSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted databases is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (isResource(resource, "aws_db_instance") && !isStorageEncrypted(resource)) {
      ctx.reportIssue(resource.labels().get(0), MESSAGE);
    }
  }

  private static final boolean isStorageEncrypted(BlockTree resource) {
    return PropertyUtils.value(resource, "storage_encrypted").filter(TextUtils::isValueTrue).isPresent();
  }
}
