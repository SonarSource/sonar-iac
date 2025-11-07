/*
 * SonarQube IaC Plugin
 * Copyright (C) 2021-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource Sàrl.
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

@Rule(key = "S6330")
public class UnencryptedSqsQueueCheck extends AbstractResourceCheck {

  private static final String MESSAGE = "Setting \"SqsManagedSseEnabled\" to \"false\" disables SQS queues encryption. Make sure it is safe here.";

  @Override
  protected void checkResource(CheckContext ctx, Resource resource) {
    if (resource.isType("AWS::SQS::Queue")) {
      if (PropertyUtils.has(resource.properties(), "KmsMasterKeyId").isTrue()) {
        return;
      }

      PropertyUtils.get(resource.properties(), "SqsManagedSseEnabled")
        .filter(property -> TextUtils.isValueFalse(property.value()))
        .ifPresent(property -> ctx.reportIssue(property, MESSAGE));
    }
  }

}
