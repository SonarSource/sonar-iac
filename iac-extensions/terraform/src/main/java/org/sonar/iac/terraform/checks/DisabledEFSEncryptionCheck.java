/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.terraform.checks;

import java.util.Optional;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.checks.SecondaryLocation;
import org.sonar.iac.common.api.tree.PropertyTree;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.api.tree.BlockTree;

@Rule(key = "S6332")
public class DisabledEFSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted EFS file systems is safe here.";
  private static final String SECONDARY_MESSAGE = "Related file system";

  @Override
  protected void checkResource(CheckContext ctx, BlockTree resource) {
    if (!isResource(resource, "aws_efs_file_system")) {
      return;
    }
    Tree resourceType = resource.labels().get(0);
    Optional<PropertyTree> maybeEncryption = PropertyUtils.get(resource, "encrypted");
    if (maybeEncryption.isPresent()) {
      PropertyTree encryption = maybeEncryption.get();
      if (TextUtils.isValueFalse(encryption.value())) {
        ctx.reportIssue(encryption.key(), MESSAGE, new SecondaryLocation(resourceType, SECONDARY_MESSAGE));
      }
    } else {
      ctx.reportIssue(resourceType, MESSAGE);
    }
  }
}
