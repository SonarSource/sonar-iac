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
package org.sonar.iac.terraform.checks;

import org.sonar.check.Rule;
import org.sonar.iac.common.checks.TextUtils;
import org.sonar.iac.terraform.symbols.ResourceSymbol;

@Rule(key = "S6330")
public class UnencryptedSqsQueueCheck extends AbstractNewResourceCheck {
  private static final String MESSAGE = "Setting \"SqsManagedSseEnabled\" to \"false\" disables SQS queues encryption. Make sure it is safe here.";

  @Override
  protected void registerResourceConsumer() {
    register("aws_sqs_queue", UnencryptedSqsQueueCheck::checkSqsQueue);
  }

  private static void checkSqsQueue(ResourceSymbol resource) {
    if (resource.attribute("kms_master_key_id").isPresent()) {
      return;
    }

    resource.attribute("sqs_managed_sse_enabled")
      .reportIf(TextUtils::isValueFalse, MESSAGE);
  }
}
