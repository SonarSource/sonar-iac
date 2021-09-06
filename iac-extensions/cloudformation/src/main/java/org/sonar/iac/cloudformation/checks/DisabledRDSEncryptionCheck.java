/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6303")
public class DisabledRDSEncryptionCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted databases is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::RDS::DBInstance") && !isStorageEncrypted(resource)) {
      ctx.reportIssue(resource.type(), MESSAGE);
    }
  }

  private static final boolean isStorageEncrypted(Resource resource) {
    return PropertyUtils.value(resource.properties(), "StorageEncrypted").filter(TextUtils::isValueTrue).isPresent();
  }
}
