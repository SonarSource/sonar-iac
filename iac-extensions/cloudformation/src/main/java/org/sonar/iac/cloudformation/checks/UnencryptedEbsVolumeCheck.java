/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource SA
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
import org.sonar.iac.common.checks.PropertyUtils;
import org.sonar.iac.common.checks.TextUtils;

@Rule(key = "S6275")
public class UnencryptedEbsVolumeCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Make sure that using unencrypted volumes is safe here.";
  private static final String OMITTING_MESSAGE = "Omitting \"Encrypted\" disables volumes encryption. Make sure it is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (!resource.isType("AWS::EC2::Volume")) {
      return;
    }

    PropertyUtils.value(resource.properties(), "Encrypted").ifPresentOrElse(
      encryptedValue -> {
        if (TextUtils.isValueFalse(encryptedValue)) {
          ctx.reportIssue(encryptedValue, MESSAGE);
        }
      }, () -> reportResource(ctx, resource, OMITTING_MESSAGE));
  }

}
