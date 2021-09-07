/*
 * Copyright (C) 2021-2021 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package org.sonar.iac.cloudformation.checks;

import java.util.Optional;
import org.sonar.check.Rule;
import org.sonar.iac.common.api.checks.CheckContext;
import org.sonar.iac.common.api.tree.Tree;
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6275")
public class UnencryptedEbsVolumeCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted volumes is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::EC2::Volume")) {
      return;
    }
    Optional<Tree> property = PropertyUtils.value(resource.properties(), "Encrypted");
    if (!property.isPresent()) {
      ctx.reportIssue(resource.type(), MESSAGE);
    } else {
      Tree encryptedValue = property.get();
      if (TextUtils.isValueFalse(encryptedValue)) {
        ctx.reportIssue(encryptedValue, MESSAGE);
      }
    }
  }

}
